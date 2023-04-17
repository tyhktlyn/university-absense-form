const ses = new AWS.SES();

const reject = async (req, res) => {

    try {
        const formId = req.body.form_id;
        // get the student details from the form id
        const studentRecord = 'something';
        await absenseCheck(studentRecord);
        console.log('Rejection complete.');
    } catch (err) {
        console.log(err);
        return res.status(500);
    }
}

const absenseCheck = (studentRecord) => {

    if (studentRecord.attendanceRate > 0.5) {
        emailStudentWarning(studentRecord);
        return;
    } else {
        emailStudentReason(studentRecord);
        return;
    }
}

const emailStudentWarning = async (studentRecord) => {
    const subject = `Absense Request Update`;
    const body = `To ${studentRecord.first_name} ${studentRecord.surame}, Your absense request had been rejected. This is an attendance warning because your attendance has dropped below 50%.`;
    const recipient = `${studentRecord.student_email}`;
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
    const body = `To ${studentRecord.first_name} ${studentRecord.surame}, Your absense request had been rejected for reason: ${}. Please discuss the matter with your module leader.`;
    const recipient = `${studentRecord.student_email}`;
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
        Source: 'your-ses-email-address'
    };

    return ses.sendEmail(params).promise();
}

module.exports = {
    reject
}