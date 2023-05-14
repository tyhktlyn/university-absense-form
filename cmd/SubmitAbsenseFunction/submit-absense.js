var mysql = require("mysql2/promise");
var AWS = require("aws-sdk");
var logger = console;
logger.info("Generate database token...");

const submit = async (req, res) => {

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

        const object = req.body;

        const result = await database_connection.query(
            `INSERT INTO requests (student_id, employee_id, module_id, start_date, end_date, absence_reason, approved) VALUES 
                (${object.student_id}, ${object.employee_id}, ${object.module_id}, ${object.start_date}, ${object.end_date}, ${object.absence_reason}, ${object.approved}`
        );
        logger.info(`Inserted data into request table`);
        console.log(result);

        return res
            .json(result)
            .status(200);

    } catch (err) {
        return res.status(500);
    }
}

module.exports = {
    submit
}