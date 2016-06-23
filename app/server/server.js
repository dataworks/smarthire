var express = require("express");
var bodyParser = require("body-parser");
var elasticsearch = require("elasticsearch");
var root = express();
var app = express();

var applicantService = require("./services/applicants.js");
var labelService = require("./services/labels.js");
var attachmentService = require("./services/attachments.js");
var uploadService = require("./services/uploads.js");
var suggestService = require("./services/suggest.js")

app.use(express.static("client"));

app.use(express.static("node_modules"));

app.use(bodyParser.urlencoded({
  extended: true
}));

app.use(bodyParser.json());

app.get("/service/applicants", function(req, res) {
  var type = null;
  if (!req.query || !req.query.type) {
    type = "new";
  } else {
    type = req.query.type.toLowerCase();
  }
  applicantService.listApplicants(req, res, type);
});

//code for favorites, changes between favorite and archive--REQUIRED
app.post("/service/labels", function(req, res) {
  labelService.index(req, res);
});

app.delete("/service/labels/:id", function(req, res) {
  labelService.delete(req, res);
});

app.get("/service/attachments", function(req, res) {
  attachmentService.getAttachment(req, res);
});

app.post("/service/uploads", function(req,res) {
  uploadService.index(req,res);
});

app.get("/service/suggest", function(req, res) {
  suggestService.suggest(req, res);
});

root.get("/", function(req, res) {
  res.redirect("/app");
});

root.use('/app', app);

//HTML5 mode, gets rid of the '#' in URLs
app.all('/*', function(req, res) {
  res.sendFile('index.html', {
    root: __dirname + "/../client/"
  });
});

var server = root.listen(8082, function() {
  var host = server.address().address
  var port = server.address().port

  console.log("ResCheck listening at http://%s:%s", host, port)
});