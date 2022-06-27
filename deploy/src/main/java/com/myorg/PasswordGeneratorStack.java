package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.BundlingOutput;
import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.DockerVolume;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.BasePathMappingOptions;
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
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.BucketAccessControl;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Permission;

import java.util.Arrays;
import java.util.List;
import static java.util.Collections.singletonList;

public class PasswordGeneratorStack extends Stack {
    public PasswordGeneratorStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public PasswordGeneratorStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        final Bucket PasswordFileBucket = Bucket.Builder.create(this, "passwordFileBucket")
        .bucketName("password-file-bucket")
        .encryption(BucketEncryption.S3_MANAGED)
        .versioned(true)
        .build();

        List<String> generatorFunctionPackagingInstructions = Arrays.asList(
            "/bin/sh",
            "-c",
            "cd GeneratorFunction " +
            "&& mvn clean install " +
            "&& cp /asset-input/GeneratorFunction/target/GeneratorFunction-0.1.jar /asset-output/"
        );

        List<String> retrieverFunctionPackagingInstructions = Arrays.asList(
            "/bin/sh",
            "-c",
            "cd RetrieverFunction " +
            "&& mvn clean install " +
            "&& cp /asset-input/RetrieverFunction/target/RetrieverFunction-0.1.jar /asset-output/"
        );

        BundlingOptions.Builder builderOptions = BundlingOptions.builder()
        .image(Runtime.JAVA_11.getBundlingImage())
        .volumes(singletonList(
            DockerVolume.builder()
                .hostPath(System.getProperty("user.home") + "/.m2/")
                .containerPath("/root/.m2/")
                .build()
            ))
        .user("root")
        .outputType(BundlingOutput.ARCHIVED);

        final Function PasswordGeneratorFunction = Function.Builder.create(this, "passwordGeneratorFunction")
        .runtime(Runtime.JAVA_11)
        .functionName("password-generator-function")
        .memorySize(1536)
        .timeout(Duration.seconds(45))
        .code(Code.fromAsset("../cmd/", AssetOptions.builder()
                        .bundling(builderOptions
                                .command(generatorFunctionPackagingInstructions)
                                .build())
                        .build()))
        .handler("generator.Generate")
        .build();

        PasswordGeneratorFunction.addPermission("passwordGeneratorFunctionApiInvokePermission", 
        Permission.builder()
        .action("lambda:InvokeFunction")
        .principal(new ServicePrincipal("apigateway.amazonaws.com")).build());

        final PolicyStatement generatorFunctionPolicy = PolicyStatement.Builder.create()
        .actions(Arrays.asList("s3:PutObject", "s3:GetObject"))
        .resources(Arrays.asList(PasswordFileBucket.getBucketArn() + "/*")).build();

        PasswordGeneratorFunction.addToRolePolicy(generatorFunctionPolicy);

        final Function PasswordRetrievalFunction = Function.Builder.create(this, "passwordRetrievalFunction")
        .runtime(Runtime.JAVA_11)
        .functionName("password-retrieval-function")
        .memorySize(1536)
        .timeout(Duration.seconds(45))
        .code(Code.fromAsset("../cmd/", AssetOptions.builder()
                        .bundling(builderOptions
                                .command(retrieverFunctionPackagingInstructions)
                                .build())
                        .build()))
        .handler("retriever.Retrieve")
        .build();

        PasswordRetrievalFunction.addPermission("passwordRetrievalFunctionApiInvokePermission", 
        Permission.builder()
        .action("lambda:InvokeFunction")
        .principal(new ServicePrincipal("apigateway.amazonaws.com")).build());

        final PolicyStatement retrieveFunctionPolicy = PolicyStatement.Builder.create()
        .actions(Arrays.asList("s3:GetObject"))
        .resources(Arrays.asList(PasswordFileBucket.getBucketArn() + "/*")).build();

        PasswordRetrievalFunction.addToRolePolicy(retrieveFunctionPolicy);

        final String apiCertArn = StringParameter.fromStringParameterName(this, "apiCertArn", "api-cert-arn").getStringValue();

        final DomainName apiDomainName = DomainName.Builder.create(this, "passwordGeneratorApiDomainName")
        .domainName("api.tracd-projects.uk")
        .certificate(Certificate.fromCertificateArn(this, "apiAuthenticationCert", apiCertArn))
        .endpointType(EndpointType.EDGE)
        .securityPolicy(SecurityPolicy.TLS_1_2).build();

        final RestApi passwordGeneratorApi = RestApi.Builder.create(this, "passwordGeneratorApi")
        .restApiName("password-generator-api")
        .defaultCorsPreflightOptions(CorsOptions.builder()
        .allowHeaders(Arrays.asList("Content-Type"))
        .allowOrigins(Arrays.asList("https://password-generator.tracd-projects.uk"))
        .allowMethods(Arrays.asList("GET", "PUT", "OPTIONS"))
        .allowCredentials(true)
        .build())
        .build();

        apiDomainName.addBasePathMapping(passwordGeneratorApi, BasePathMappingOptions.builder().basePath("password-generator").build());

        final Resource retrievePath = passwordGeneratorApi.getRoot().addResource("retrieve");
        final Resource generatePath = passwordGeneratorApi.getRoot().addResource("generate");

        retrievePath.addMethod("GET", new LambdaIntegration(PasswordRetrievalFunction));
        generatePath.addMethod("GET", new LambdaIntegration(PasswordGeneratorFunction));

        final Bucket webAssetsBucket = Bucket.Builder.create(this, "passwordGeneratorWebAssetsBucket")
        .bucketName("password-generator-web-assets-bucket")
        .removalPolicy(RemovalPolicy.DESTROY)
        .accessControl(BucketAccessControl.PRIVATE)
        .build();

        final OriginAccessIdentity cloudFrontS3AccessIdentity = OriginAccessIdentity.Builder.create(this, "webAssetsBucketAccessIdentity").build();

        webAssetsBucket.grantRead(cloudFrontS3AccessIdentity);

        final Distribution webDistribution = Distribution.Builder.create(this, "cloudFrontWebDistribution")
        .defaultRootObject("Homepage.html")
        .defaultBehavior(BehaviorOptions.builder()
            .origin(new S3Origin(webAssetsBucket, S3OriginProps.builder()
                .originAccessIdentity(cloudFrontS3AccessIdentity)
                .build()))
            .build())
        .domainNames(Arrays.asList("password-generator.tracd-projects.uk"))
        .certificate(Certificate.fromCertificateArn(this, "cloudFrontWebAuthenticationCert", apiCertArn))
        .build();

        BucketDeployment.Builder.create(this, "webDeploymentBucket")
        .sources(Arrays.asList(Source.asset("../cmd/ui")))
        .destinationBucket(Bucket.fromBucketArn(this, "bucket", webAssetsBucket.getBucketArn()))
        .distribution(webDistribution)
        .build();
    }
}
