const express = require('express');
const bodyParser = require('body-parser'); 
const app = express();
const port = 1234;
const cors = require('cors');

const { approve } = require("./cmd/ApproveAbsenseFunction/approve-absense");
const { reject } = require("./cmd/RejectAbsenseFunction/reject-absense");
const { retreive } = require("./cmd/RetrieveAbsenseFunction/retrieve-absense");
const { submit } = require("./cmd/SubmitAbsenseFunction/submit-absense");

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(cors());

app.post("/approve", approve);
app.post("/reject", reject);
app.get("/retrieve", retreive);
app.post("/submit", submit);

app.listen(port, () => { console.log(`Server started on port ${port}`)});