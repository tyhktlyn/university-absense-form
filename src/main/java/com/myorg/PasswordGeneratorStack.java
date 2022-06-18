package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.EndpointType;
import software.amazon.awscdk.services.apigateway.SecurityPolicy;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.OriginAccessIdentity;
import software.amazon.awscdk.services.cloudfront.origins.S3Origin;
import software.amazon.awscdk.services.cloudfront.origins.S3OriginProps;
import software.amazon.awscdk.services.apigateway.DomainName;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.BucketAccessControl;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Permission;

import java.util.Arrays;

public class PasswordGeneratorStack extends Stack {
    public PasswordGeneratorStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public PasswordGeneratorStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        final Bucket PasswordFileBucket = Bucket.Builder.create(this, "passwordFileBucket")
        .bucketName("password-file-bucket")
        .versioned(true)
        .build();

        final Function PasswordGeneratorFunction = Function.Builder.create(this, "passwordGeneratorFunction")
        .runtime(Runtime.JAVA_8)
        .functionName("password-generator-function")
        .timeout(Duration.seconds(45))
        .code(Code.fromAsset("file path to be updated here once code is written"))
        .handler("to be updated with the name of the entry point fucntion of the code")
        .build();

        PasswordGeneratorFunction.addPermission("passwordGeneratorFunctionApiInvokePermission", 
        Permission.builder()
        .action("lambda:InvokeFunction")
        .principal(new ServicePrincipal("apigateway.amazonaws.com")).build());

        final PolicyStatement generatorFunctionPolicy = PolicyStatement.Builder.create()
        .actions(Arrays.asList("s3:PutObject"))
        .resources(Arrays.asList(PasswordFileBucket.getBucketArn())).build();

        PasswordGeneratorFunction.addToRolePolicy(generatorFunctionPolicy);

        final Function PasswordRetrievalFunction = Function.Builder.create(this, "passwordRetrievalFunction")
        .runtime(Runtime.JAVA_8)
        .functionName("password-retrieval-function")
        .timeout(Duration.seconds(45))
        .code(Code.fromAsset("file path to be updated here once code is written"))
        .handler("to be updated with the name of the entry point fucntion of the code")
        .build();

        PasswordRetrievalFunction.addPermission("passwordRetrievalFunctionApiInvokePermission", 
        Permission.builder()
        .action("lambda:InvokeFunction")
        .principal(new ServicePrincipal("apigateway.amazonaws.com")).build());

        final PolicyStatement retrieveFunctionPolicy = PolicyStatement.Builder.create()
        .actions(Arrays.asList("s3:GetObject"))
        .resources(Arrays.asList(PasswordFileBucket.getBucketArn())).build();

        PasswordRetrievalFunction.addToRolePolicy(retrieveFunctionPolicy);

        final String apiCertArn = StringParameter.fromStringParameterName(this, "apiCertArn", "api-cert-arn").getStringValue();

        DomainName.Builder.create(this, "passwordGeneratorApiDomainName")
        .domainName("api.tracd-projects.uk")
        .certificate(Certificate.fromCertificateArn(this, "apiAuthenticationCert", apiCertArn))
        .endpointType(EndpointType.EDGE)
        .basePath("password-generator")
        .securityPolicy(SecurityPolicy.TLS_1_2).build();

        final RestApi passwordGeneratorApi = RestApi.Builder.create(this, "passwordGeneratorApi").restApiName("password-generator-api").build();

        final Resource retrievePath = passwordGeneratorApi.getRoot().addResource("retrieve");
        final Resource generatePath = passwordGeneratorApi.getRoot().addResource("generate");

        retrievePath.addMethod("GET", new LambdaIntegration(PasswordRetrievalFunction));
        generatePath.addMethod("PUT", new LambdaIntegration(PasswordGeneratorFunction));

        final Bucket webAssetsBucket = Bucket.Builder.create(this, "webAssetsBucket")
        .bucketName("web-assets-bucket")
        .removalPolicy(RemovalPolicy.DESTROY)
        .accessControl(BucketAccessControl.PRIVATE)
        .build();

        final OriginAccessIdentity cloudFrontS3AccessIdentity = OriginAccessIdentity.Builder.create(this, "webAssetsBucketAccessIdentity").build();

        webAssetsBucket.grantRead(cloudFrontS3AccessIdentity);

        final Distribution webDistribution = Distribution.Builder.create(this, "cloudFrontWebDistribution")
        .defaultRootObject("index.html")
        .defaultBehavior(BehaviorOptions.builder()
            .origin(new S3Origin(webAssetsBucket, S3OriginProps.builder()
                .originAccessIdentity(cloudFrontS3AccessIdentity)
                .build()))
            .build())
        .domainNames(Arrays.asList("www.password-generator.tracd-projects.uk"))
        .build();

        BucketDeployment.Builder.create(this, "webDeploymentBucket")
        .sources(Arrays.asList(Source.asset("path to ui folder")))
        .destinationBucket(Bucket.fromBucketArn(this, "bucket", webAssetsBucket.getBucketArn()))
        .distribution(webDistribution)
        .build();
    }
}
