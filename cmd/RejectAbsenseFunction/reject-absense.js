const AWS = require('aws-sdk');
const ses = new AWS.SES();

const reject = async (req, res) => {

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

        const request_id = req.body.request_id;
        const student_id = req.body.student_id;

        const studentRecord = await database_connection.query(
            `SELECT * FROM requests WHERE student_id = ${student_id}`
        );

        await absenseCheck(studentRecord);

        console.log('Rejection complete.');
    } catch (err) {
        console.log(err);
        return res.status(500);
    }
}

const absenseCheck = (studentRecord) => {

    if (studentRecord.absence_counter > 5) {
        emailStudentWarning(studentRecord);
        return;
    } else {
        emailStudentReason(studentRecord);
        return;
    }
}

const emailStudentWarning = async (studentRecord) => {
    const subject = `Absense Request Update`;
    const body = `To ${studentRecord.first_name} ${studentRecord.surame}, Your absense request had been rejected. This is an attendance warning because you have missed more than 5 sessions.`;
    const recipient = `${studentRecord.email}`;
    sendEmail(subject, body, recipient)
        .then(() => {
            console.log('Email sent successfully');
        })
        .catch((err) => {
            console.error('Email sending failed:', err);
        });
    return;
}

const emailStudentReason = async (studentRecord) => {
    const subject = `Absense Request Update`;
    const body = `To ${studentRecord.first_name} ${studentRecord.surame}, Your absense request had been rejected. Please discuss the matter with your module leader.`;
    const recipient = `${studentRecord.email}`;
    sendEmail(subject, body, recipient)
        .then(() => {
            console.log('Email sent successfully');
        })
        .catch((err) => {
            console.error('Email sending failed:', err);
        });
    return;
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
    reject
}