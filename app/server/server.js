var express = require("express");
var bodyParser = require("body-parser");
var elasticsearch = require("elasticsearch");
var root = express();
var app = express();

var esservice = require("./elasticsearch.js");

var applicantConfig = {
  url: "172.31.61.189:9200",
  index: "applicants",
  type: "applicant"
};

var labelConfig = {
  url: "172.31.61.189:9200",
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

  // var query = "name:Dave Mezzetti";
  esservice.query(labelConfig, req, res, "*", function(res, hits){
    //var ids = map source -> _id
    var query = '*';
    if (hits && hits.length > 0) {
      var ids = hits.map(function(hit) { return hit.id; })
      //same query logic * or NOT id ()
      if (ids && ids.length > 0) {
        query = "NOT id:(" + ids.join(" ") + ")";
      }
   } 

    esservice.query(applicantConfig, req, res, query, null);
  });

},function (error, response) {
  console.log(error);
});

//code for favorites, changes between favorite and archive--REQUIRED
app.post("/service/favorites", function(req, res) {
    var client = new elasticsearch.Client({
      host: 'interns.dataworks-inc.com/elasticsearch'
    });

    var id = req.body.id;
    var type = req.body.type;

    client.index({
    index: 'labels',
    type: 'label',
    id: id,
    body: {
      id: id,
      type: type,
    }
  }, function (error, response) {
    console.log(error);
  });
});

//get code for favorites
app.get("/service/favorites", function(req, res) {
  // var query = "name:Dave Mezzetti";
  esservice.query(labelConfig, req, res, "type: favorite", function(res, hits){
    //var ids = map source -> _id
    var query = '*';
    if (hits && hits.length > 0) {
      var ids = hits.map(function(hit) { return hit.id; })

      //same query logic * or NOT id ()
      if (ids && ids.length > 0) {
        query = "id:(" + ids.join(" ") + ")"
      }
   } 

    esservice.query(applicantConfig, req, res, query, null);
  });
},function (error, response) {
  console.log(error);
});

//get code for archive
app.get("/service/archive", function(req, res) {
  // var query = "name:Dave Mezzetti";
  esservice.query(labelConfig, req, res, "type: archive", function(res, hits){
    //var ids = map source -> _id
    var query = '*';
    if (hits && hits.length > 0) {
      var ids = hits.map(function(hit) { return hit.id; })

      //same query logic * or NOT id ()
      if (ids && ids.length > 0) {
        query = "id:(" + ids.join(" ") + ")"
      }
   } 

    esservice.query(applicantConfig, req, res, query, null);
  });

},function (error, response) {
  console.log(error);
});

//code for archive, changes between favorite and archive--REQUIRED
app.post("/service/archive", function(req, res) {
    var client = new elasticsearch.Client({
      host: 'interns.dataworks-inc.com/elasticsearch'
    });

    var id = req.body.id;
    var type = req.body.type;

    client.index({
    index: 'labels',
    type: 'label',
    id: id,
    body: {
      id: id,
      type: type,
    }
  }, function (error, response) {
    console.log(error);
  });
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