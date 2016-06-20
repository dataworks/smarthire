var esservice = require("./elasticsearch.js");
var config = require("./config.js");

//creates index for labels
exports.index = function(req, res) {
	esservice.index(config.labels, req, res);
}

//calls delte method for labels
exports.delete = function(req, res) {
	esservice.delete(config.labels, req, res);
}