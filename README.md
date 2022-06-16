# Welcome to your CDK Java project!

This is a blank project for CDK development with Java.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

## Password security requirements 

* Must be at least 12 characters
* Must contain uppercase letters
* Must contain at least 2 numbers
* Must contain at least one special character
* Must not contain any repeated special characters

Example of a valid password:

company name: mcdonalds
given word: pies

password generator : Mcdonaldsellspies701{#

Example of invalid password:

company name: kfc 
given word: chips

password generator : kfcloveschips1##

Enjoy!
