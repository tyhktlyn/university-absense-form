package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import com.fasterxml.jackson.core.Versioned;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Permission;

import java.util.List;

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
        .principal(new ServicePrincipal("lambda:InvokeFunction")).build());

        final PolicyStatement lambdaFunctionPolicy = PolicyStatement.Builder.create()
        .actions(List.of("s3:GetObject", "s3:PutObject"))
        .resources(List.of(PasswordFileBucket.getBucketArn())).build();

        PasswordGeneratorFunction.addToRolePolicy(lambdaFunctionPolicy);
    }
}
