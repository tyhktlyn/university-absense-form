import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import * as iam from "aws-cdk-lib/aws-iam";
import { Bucket, BucketAccessControl } from "aws-cdk-lib/aws-s3";
import * as cloudfront from "aws-cdk-lib/aws-cloudfront";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import {
  NodejsFunction,
  NodejsFunctionProps,
} from "aws-cdk-lib/aws-lambda-nodejs";
import * as lambda from "aws-cdk-lib/aws-lambda";
import * as cdk from "aws-cdk-lib";
import * as path from "path";
import { S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";

export class AbsenseFormStack extends cdk.Stack {
  constructor(scope: cdk.App, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // create the VPC
    const vpc = new ec2.Vpc(this, "cw-vpc", {
      ipAddresses: ec2.IpAddresses.cidr("10.0.0.0/16"),
      natGateways: 0,
      maxAzs: 3,
      subnetConfiguration: [
        {
          name: "public-subnet-1",
          subnetType: ec2.SubnetType.PUBLIC,
          cidrMask: 24,
        },
        {
          name: "private-subnet-1",
          subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
          cidrMask: 28,
        },
      ],
    });

    const secretsManagerVpcEndpointSg = new ec2.SecurityGroup(
      this,
      "secrets-manager-endpoint-sg",
      {
        vpc,
        allowAllOutbound: true,
      }
    );

    const dbSg = new ec2.SecurityGroup(this, "db-sg", {
      vpc,
    });

    const lambdaSg = new ec2.SecurityGroup(this, "lambda-sg", {
      vpc,
      allowAllOutbound: true,
    });

    const secretsManagerVpcEndpoint = vpc.addInterfaceEndpoint(
      "secrets-manager-vpc-endpoint",
      {
        service: ec2.InterfaceVpcEndpointAwsService.SECRETS_MANAGER,
        subnets: { subnetType: ec2.SubnetType.PUBLIC },
        securityGroups: [secretsManagerVpcEndpointSg],
      }
    );

    // create RDS instance
    const dbInstance = new rds.DatabaseInstance(this, "db-instance", {
      instanceIdentifier: "AbsenseRecords",
      vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
      },
      engine: rds.DatabaseInstanceEngine.mysql({
        version: rds.MysqlEngineVersion.VER_8_0_32,
      }),
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.BURSTABLE3,
        ec2.InstanceSize.MICRO
      ),
      credentials: rds.Credentials.fromGeneratedSecret("admin"),
      multiAz: false,
      allocatedStorage: 100,
      maxAllocatedStorage: 110,
      allowMajorVersionUpgrade: false,
      autoMinorVersionUpgrade: true,
      backupRetention: cdk.Duration.days(0),
      deleteAutomatedBackups: true,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      deletionProtection: false,
      databaseName: "AbsenseRecordsDB",
      publiclyAccessible: false,
      securityGroups: [dbSg],
    });

    const lambdaDBRole = new iam.Role(this, "LambdaProxyRole", {
      assumedBy: new iam.ServicePrincipal("lambda.amazonaws.com"),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName(
          "service-role/AWSLambdaVPCAccessExecutionRole"
        ),
      ],
    });

    lambdaDBRole.addToPolicy(
      new iam.PolicyStatement({
        effect: iam.Effect.ALLOW,
        actions: ["rds-db:connect"],
        resources: [dbInstance.instanceArn],
      })
    );

    dbInstance.secret!.grantRead(lambdaDBRole);

    // create default lambda propd
    const nodeJsFunctionProps: NodejsFunctionProps = {
      bundling: {
        nodeModules: ["mysql2"],
        externalModules: ["aws-sdk"],
      },
      runtime: lambda.Runtime.NODEJS_16_X,
      timeout: cdk.Duration.minutes(3),
      memorySize: 256,
      role: lambdaDBRole,
      vpc,
      vpcSubnets: vpc.selectSubnets({
        subnetType: ec2.SubnetType.PUBLIC,
      }),
      securityGroups: [lambdaSg],
    };

    const lambdaFn = new NodejsFunction(this, "lambdaFn", {
      entry: path.join(
        __dirname,
        "../../cmd/SetupDataFunction",
        "setup-data.js"
      ),
      ...nodeJsFunctionProps,
      functionName: "LambdaFn",
      allowPublicSubnet: true,
      environment: {
        DB_SECRET_NAME: dbInstance.secret?.secretName ?? "",
      },
    });

    // allow proxy to access db
    dbInstance.connections.allowFrom(
      lambdaFn.connections,
      ec2.Port.tcp(3306),
      "Allow connections from lambda to db"
    );

    // Allow outgoing traffic on the database port to the RDS instance security group
    lambdaFn.connections.allowTo(
      dbInstance.connections,
      ec2.Port.tcp(3306),
      "Allow connections to db from lambda"
    );
    lambdaFn.connections.allowFrom(
      secretsManagerVpcEndpointSg.connections,
      ec2.Port.tcp(3306),
      "Allow responses from secrets manager vpce"
    );

    secretsManagerVpcEndpointSg.connections.allowFrom(
      lambdaFn.connections,
      ec2.Port.tcp(443),
      "allow access from lambda in public vpc"
    );
    dbInstance.grantConnect(lambdaFn);

    const webAssetsBucket = new Bucket(
      this,
      "absenseFormWebAssets",
      {
        bucketName: "absense-form-web-assets-bucket",
        removalPolicy: cdk.RemovalPolicy.DESTROY,
        accessControl: BucketAccessControl.PRIVATE,
      }
    );

    const cloudFrontS3AccessIdentity = new cloudfront.OriginAccessIdentity(
      this,
      "webAssetsBucketAccessIdentity"
    );

    webAssetsBucket.grantRead(cloudFrontS3AccessIdentity);

    const webDistribution = new cloudfront.Distribution(
      this,
      "cloudFrontWebDistribution",
      {
        defaultRootObject: "index.html",
        defaultBehavior: {
          origin: new S3Origin(webAssetsBucket, {
            originAccessIdentity: cloudFrontS3AccessIdentity
          })
        }
      }
    );

    const sourceDirectory = '../../cmd/ui';
    new BucketDeployment(this, "webDeploymentBucket", {
      sources: [Source.asset(sourceDirectory)],
      destinationBucket: webAssetsBucket,
      distribution: webDistribution,
      distributionPaths: ["/*"],
    });

    new cdk.CfnOutput(this, "dbEndpoint", {
      value: dbInstance.instanceEndpoint.hostname,
    });

    new cdk.CfnOutput(this, "secretName", {
      // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
      value: dbInstance.secret?.secretName!,
    });

    new cdk.CfnOutput(this, "DistributionDomainName", {
      value: webDistribution.domainName,
    });  
  }
}
