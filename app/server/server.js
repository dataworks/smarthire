var express = require("express");
var bodyParser = require("body-parser");
var elasticsearch = require("elasticsearch");
var root = express();
var app = express();

var esservice = require("./elasticsearch.js");

var applicantConfig = {
  url: "interns.dataworks-inc.com/elasticsearch",
  index: "applicants",
  type: "applicant"
};

var labelConfig = {
  url: "interns.dataworks-inc.com/elasticsearch",
  index: "labels",
  type: "label"
};

app.use(express.static("client"));

app.use(express.static("node_modules"));

app.use(bodyParser.urlencoded({
    extended: true
}));

app.use(bodyParser.json());

app.get("/service/applicants", function(req, res) {
  esservice.query(labelConfig, req, res, "*", function(res, hits){
    //var ids = map source -> _id
    var query = esservice.map(res, hits, 'applicant');
    esservice.query(applicantConfig, req, res, query, null);
  });
},function (error, response) {
  console.log(error);
});

//code for favorites, changes between favorite and archive--REQUIRED
app.post("/service/favorites", function(req, res) {
  esservice.index(req, res, labelConfig);
});

//get code for favorites
app.get("/service/favorites", function(req, res) {
  esservice.query(labelConfig, req, res, "type: favorite", function(res, hits){
    //var ids = map source -> _id
    var query = esservice.map(res, hits, 'favorite');
    esservice.query(applicantConfig, req, res, query, null);
  });
},function (error, response) {
  console.log(error);
});

//get code for archive
app.get("/service/archive", function(req, res) {
  esservice.query(labelConfig, req, res, "type: archive", function(res, hits){
    //var ids = map source -> _id
    var query = esservice.map(res, hits, 'archive');
    esservice.query(applicantConfig, req, res, query, null);
  });

},function (error, response) {
  console.log(error);
});

//code for archive, changes between favorite and archive--REQUIRED
app.post("/service/archive", function(req, res) {
  esservice.index(req, res, labelConfig);
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

  console.log("Example app listening at http://%s:%s", host, port)

});


//   res.json([{
//     "name": "Joe Schweitzer",
//     "score": 0.99,
//     "currentEntity": "Data Works Inc.",
//     "currentLocation": "Reston, VA",
//     "skills": ["Grails", "Groovy", "Ext JS"]
//   }, {
//     "name": "Laura Schweitzer",
//     "score": 0.98,
//     "currentEntity": "Data Works Inc.",
//     "currentLocation": "Reston, VA",
//     "skills": ["Pentaho", "Ruby on Rails", "Javascript"]
//   }, {
//     "name": "Dave Mezzetti",
//     "score": 0.30,
//     "currentEntity": "Data Works Inc.",
//     "currentLocation": "Reston, VA",
//     "skills": ["Scala", "Spark", "Tweeting"]
//   }, {
//     "name": "Dennis Schweitzer",
//     "score": 0.58,
//     "currentEntity": "Data Works Inc.",
//     "currentLocation": "Reston, VA",
//     "skills": ["Pentaho", "Ruby on Rails", "Javascript"]
//   }]);
// });