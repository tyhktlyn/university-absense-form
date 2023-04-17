const client = require("../../config");

const getRecordsByTeacher = async (teacher_email) => {
    const params = {
        database: client.params.database,
        resourceArn: client.params.resourceArn,
        secretArn: client.params.secretArn,
        sql: 'SELECT * FROM AbsenceRequests WHERE teacher_email = :teacher_email',
        parameters: [
            { name: 'teacher_email', value: { stringValue: teacher_email } }
        ]
    };

    try {
        const result = await client.executeStatement(params).promise();
        console.log(result);
        return result.records;
    } catch (err) {
        console.error(err);
        throw err;
    }
}

const retreive = async (req, res) => {

    try {

        const teacher_email = req.body.teacher_email;
        const response = await getRecordsByTeacher(teacher_email);

        return res
            .json(response)
            .status(200);

    } catch (err) {
        return res.status(500);
    }
}

module.exports = {
    retreive
}