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
        await database_connection.query(
            `CREATE TABLE IF NOT EXISTS students (
            student_id VARCHAR(9) NOT NULL AUTO_INCREMENT,
            first_name VARCHAR(255) NOT NULL,
            surname VARCHAR(255) NOT NULL,
            absence_counter INT(2) NOT NULL,
            year_of_study INT(1) NOT NULL,
            email VARCHAR(255) NOT NULL,
            PRIMARY KEY (student_id))`
        );
        logger.info('Created table "students"');
        await database_connection.query(
            `CREATE TABLE IF NOT EXISTS teachers (
            employee_id VARCHAR(9) NOT NULL AUTO_INCREMENT,
            first_name VARCHAR(255) NOT NULL,
            surname VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            PRIMARY KEY (employee_id))`
        );
        logger.info('Created table "teachers"');
        await database_connection.query(
            `CREATE TABLE IF NOT EXISTS requests (
            request_id INT(9) NOT NULL AUTO_INCREMENT,
            student_id INT(9) NOT NULL,
            employee_id INT(9) NOT NULL,
            module_id INT(9) NOT NULL,
            start_date DATETIME NOT NULL,
            end_date DATETIME NOT NULL,
            absence_reason VARCHAR(500) NOT NULL,
            approved ENUM(0, 1) NOT NULL,
            PRIMARY KEY (request_id))`
        );
        logger.info('Created table "requests"');
        await database_connection.query(
            `CREATE TABLE IF NOT EXISTS modules (
            module_id INT(9) NOT NULL AUTO_INCREMENT,
            module_name VARCHAR(255) NOT NULL,
            PRIMARY KEY (module_id))`
        );
        logger.info('Created table "modules"');
        await database_connection.query(`INSERT INTO students (student_id, first_name, surname, absence_counter, year_of_study, email) VALUES
              ('u2086699', 'Daniel', 'Bolarinwa', 0, 3, 'u2086699@uel.ac.uk'),
              ('u2086635', 'Teyah', 'Davis', 0, 3, 'u2086635@uel.ac.uk'),
              ('u2090262', 'Jasmine', 'Wells', 0, 3, 'u2090262@uel.ac.uk')`
            );
        logger.info('Inserted 3 new records into table "student"');
        await database_connection.query(`INSERT INTO teachers (employee_id, first_name, surname, email) VALUES
              ('t2045898', 'Aloysius', 'Edoh', 't2045898@uel.ac.uk')`
            );
        logger.info('Inserted 1 new records into table "teachers"');
        await database_connection.query(`INSERT INTO modules (module_id, module_name) VALUES
              ('CD6001', 'Enterprise Architecture')`
            );
        logger.info('Inserted 1 new records into table "modules"');
    } catch (error) {
        console.error("Error:", error);
    } finally {
        await database_connection.end();
        logger.info("Disconnected from RDS proxy endpoint");
    }
};
