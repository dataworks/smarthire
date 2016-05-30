var express = require("express");
var elasticsearch = require("elasticsearch");
var app = express();

app.use(express.static("client"));
app.use(express.static("node_modules"));

app.get("/applicants", function(req, res) {
  res.json([{
    "name": "Joe Schweitzer",
    "score": 0.99,
    "currentEntity": "Data Works Inc.",
    "currentLocation": "Reston, VA",
    "skills": ["Grails", "Groovy", "Ext JS"]
  }, {
    "name": "Laura Schweitzer",
    "score": 0.98,
    "currentEntity": "Data Works Inc.",
    "currentLocation": "Reston, VA",
    "skills": ["Pentaho", "Ruby on Rails", "Javascript"]
  }, {
    "name": "Dave Mezzetti",
    "score": 0.10,
    "currentEntity": "Data Works Inc.",
    "currentLocation": "Reston, VA",
    "skills": ["Scala", "Spark", "Tweeting"]
  }]);
});

app.get("/search", function(req, res) {
  var client = new elasticsearch.Client({
    host: 'interns.dataworks-inc.com/elasticsearch'
  });

  client.search({
    index: 'test',
    q: req.params.query || '*'
  }).then(function (body) {
    var hits = body.hits.hits.map(function(hit) { return hit._source; });;
    res.json(hits);
  }, function (error) {
    console.trace(error.message);
  });
});

var server = app.listen(8082, function () {
  var host = server.address().address
  var port = server.address().port

  console.log("Example app listening at http://%s:%s", host, port)

})
