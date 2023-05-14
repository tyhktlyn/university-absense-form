var mysql = require("mysql2/promise");
var AWS = require("aws-sdk");
var logger = console;
logger.info("Generate database token...");

const retreive = async (req, res) => {

    const dbSecretName = process.env.DB_SECRET_NAME || '';
    const client = new AWS.SecretsManager({
        region: "eu-west-2"
    });

    // Retrieve the secret value
    const secretValue = await client.getSecretValue({ SecretId: dbSecretName }).promise();

    let secretJson = ""
    if ('SecretString' in secretValue) {
        secretJson = JSON.parse(secretValue.SecretString);
    } else {
        secretJson = JSON.parse(Buffer.from(secretValue.SecretBinary, 'base64').toString('ascii'));
    }

    if (!secretJson) {
        throw new Error('secret string is empty');
    }

    logger.info(`secret successfully obtained. Connecting to database...`);
    const database_connection = await mysql.createConnection({
        host: secretJson.host,
        port: parseInt(secretJson.port),
        database: secretJson.dbname,
        user: secretJson.username,
        password: secretJson.password,
    });
    logger.info("Connected!!");

    try {

        const employee_id = req.body.employee_id;

        const result = await database_connection.query(
            `SELECT * FROM requests WHERE employee_id = ${employee_id}`
        );
        logger.info(`Read data from request table`);
        
        console.log(result);

        return res
            .json(result.records)
            .status(200);

    } catch (err) {
        return res.status(500);
    }
}

module.exports = {
    retreive
}