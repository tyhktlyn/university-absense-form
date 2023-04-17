const ses = new AWS.SES();

const approve = async (req, res) => {

    try {
        await emailStudent;
        await updateRecord;
    } catch (err) {
        console.log(err);
        return res.status(500);
    }
}

const emailStudent = async (studentRecord) => {
    const subject = `Absense Request Update`;
    const body = `To ${studentRecord.first_name} ${studentRecord.surame}, Your absense request had been approved.`;
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

const updateRecord = async () => { // both the request and attendance rate

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
    approve
}