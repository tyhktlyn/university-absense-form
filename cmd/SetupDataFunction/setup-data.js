// ../cmd/SetupDataFunction/setup-data.js
var mysql = require("mysql2/promise");
var AWS = require("aws-sdk");
var logger = console;
logger.info("Generate database token...");
exports.handler = async (event, context) => {
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
        await database_connection.query(`CREATE TABLE IF NOT EXISTS users (
              id INT(11) NOT NULL AUTO_INCREMENT,
              name VARCHAR(255) NOT NULL,
              age INT(11) NOT NULL,
              email VARCHAR(255) NOT NULL,
              PRIMARY KEY (id))`);
        logger.info('Created table "users"');
        await database_connection.query(`INSERT INTO users (name, age, email) VALUES
              ('John', 30, 'john@example.com'),
              ('Jane', 25, 'jane@example.com'),
              ('Bob', 40, 'bob@example.com')`);
        logger.info('Inserted 3 new records into table "users"');
    } catch (error) {
        console.error("Error:", error);
    } finally {
        await database_connection.end();
        logger.info("Disconnected from RDS proxy endpoint");
    }
};
