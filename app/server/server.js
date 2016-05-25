var express = require('express');
var app = express();

app.use(express.static('client'));
app.use(express.static('node_modules'));

app.get('/applicants', function(req, res) {
  res.json([{
    name: 'Joe Schweitzer',
    score: 0.99,
    currentEntity: 'Data Works Inc.',
    currentLocation: 'Reston, VA',
    skills: ['Grails', 'Groovy', 'Ext JS']
  }, {
    name: 'Laura Schweitzer',
    score: 0.45,
    currentEntity: 'Data Works Inc.',
    currentLocation: 'Reston, VA',
    skills: ['Pentaho', 'Ruby on Rails', 'Javascript']
  }]);
});

var server = app.listen(8082, function () {
  var host = server.address().address
  var port = server.address().port

  console.log("Example app listening at http://%s:%s", host, port)

})
