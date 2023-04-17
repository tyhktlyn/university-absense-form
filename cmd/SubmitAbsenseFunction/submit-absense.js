const client = require("../../config");

const insertObject = async (object) => {
    const params = {
        database: client.params.database,
        resourceArn: client.params.resourceArn,
        secretArn: client.params.secretArn,
        sql: 'INSERT INTO AbsenceRequests (student_email, teacher_email, module_id, start_date_time, end_date_time, absence_reason, accepted_rejected) VALUES (:student_email, :teacher_email, :module_id, :start_date_time, :end_date_time, :absence_reason, :accepted_rejected)',
        parameters: [
            { name: 'student_email', value: { stringValue: object.student_email.toString() } },
            { name: 'teacher_email', value: { stringValue: object.teacher_email.toString() } },
            { name: 'module_id', value: { stringValue: object.module_id.toString() } },
            { name: 'start_date_time', value: { stringValue: object.start_date_time.toString() } },
            { name: 'end_date_time', value: { stringValue: object.end_date_time.toString() } },
            { name: 'absence_reason', value: { stringValue: object.absence_reason.toString() } },
            { name: 'accepted_rejected', value: { stringValue: object.accepted_rejected.toString() } }
        ]
    };

    try {
        const result = await client.executeStatement(params).promise();
        console.log(result);
        return result;
    } catch (err) {
        console.error(err);
        throw err;
    }
}

const submit = async (req, res) => {

    try {

        // const student_email = req.body.student_email;
        // const teacher_email = req.body.teacher_email;
        // const module_id = req.body.module_id;
        // const start_date_time = req.body.start_date_time;
        // const end_date_time = req.body.end_date_time;
        // const absence_reason = req.body.absence_reason;
        // const accepted_rejected = req.body.accepted_rejected;

        // const record = {
        //     student_email: student_email,
        //     teacher_email: teacher_email,
        //     module_id: module_id,
        //     start_date_time: start_date_time,
        //     end_date_time: end_date_time,
        //     absence_reason: absence_reason,
        //     accepted_rejected: accepted_rejected
        // }

        const record = req.body;

        const response = await insertObject(record);
        return res
            .json(response)
            .status(200);

    } catch (err) {
        return res.status(500);
    }
}

module.exports = {
    submit
}