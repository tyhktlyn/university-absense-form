const AWS = require('aws-sdk');

const client = new AWS.RDSDataService({
    region: 'eu-west-2',
    credentials: {
        accessKeyId: 'your-access-key-id',
        secretAccessKey: 'your-secret-access-key'
    },
    params: {
        database: 'your-database',
        resourceArn: 'your-resource-arn',
        secretArn: 'your-secret-arn'
    }
});

module.exports = {
    client
}