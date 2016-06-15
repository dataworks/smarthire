var express = require("express");
var bodyParser = require("body-parser");
var elasticsearch = require("elasticsearch");
var root = express();
var app = express();

var applicantService = require("./services/applicants.js");
var labelService = require("./services/labels.js");

app.use(express.static("client"));

app.use(express.static("node_modules"));

app.use(bodyParser.urlencoded({
    extended: true
}));

app.use(bodyParser.json());

app.get("/service/applicants", function(req, res) {
  applicantService.listApplicants(req, res, 'applicant');
});


//code for favorites, changes between favorite and archive--REQUIRED
app.post("/service/favorites", function(req, res) {
  labelService.index(req,res);
});

//get code for favorites
app.get("/service/favorites", function(req, res) {
    applicantService.listApplicants(req, res, 'favorite');
});

//get code for archive
app.get("/service/archive", function(req, res) {
   applicantService.listApplicants(req, res, 'archive');
});

//code for archive, changes between favorite and archive--REQUIRED
app.post("/service/archive", function(req, res) {
   labelService.index(req, res);
});


root.get("/", function(req, res) {
  res.redirect("/app");
});

root.use('/app', app);

//HTML5 mode
app.all('/*', function(req, res) {
  res.sendFile('index.html', {root: __dirname + "/../client/"});
});

var server = root.listen(8082, function () {
  var host = server.address().address
  var port = server.address().port

  console.log("ResCheck listening at http://%s:%s", host, port)

});
