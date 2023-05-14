const AWS = require('aws-sdk');
const ses = new AWS.SES();

const approve = async (req, res) => {

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

        const student_id = req.params.student_id;

        const studentRecord = await database_connection.query(
            `SELECT * FROM students WHERE student_id = ${student_id}`
        );
        logger.info(`Read data from student table`);

        console.log(studentRecord);

        const subject = `Absense Request Update`;
        const body = `To ${studentRecord.first_name} ${studentRecord.surame}, Your absense request had been approved.`;
        const recipient = `${studentRecord.email}`;

        sendEmail(subject, body, recipient)
            .then(() => {
                console.log('Email sent successfully');
            })
            .catch((err) => {
                console.error('Email sending failed:', err);
            });

        await database_connection.query(
            `UPDATE student
            SET student_id = ${studentRecord.student_id}, first_name = ${studentRecord.first_name}, surname = ${studentRecord.surame}, absence_counter = ${studentRecord.absence_counter + 1}, year_of_study = ${studentRecord.year_of_study}, email = ${studentRecord.email}
            WHERE student_id = ${student_id}`
        );
        logger.info(`Updated data in student table`);

    } catch (err) {
        console.log(err);
        return res.status(500);
    }
}

const sendEmail = async (subject, body, recipient) => {
    const params = {
        Destination: {
            ToAddresses: [recipient]
        },
        Message: {
            Body: {
                Html: {
                    Data: body
                }
            },
            Subject: {
                Data: subject
            }
        },
        Source: 'accenturewebgroup@gmail.com'
    };

    return ses.sendEmail(params).promise();
}

module.exports = {
    approve
}