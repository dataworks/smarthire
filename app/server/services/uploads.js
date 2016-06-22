var esservice = require("./elasticsearch.js");
var config = require("./config.js");

//creates index for labels
exports.index = function(req, res) {
  esservice.index(config.uploads, req.body, res);
}