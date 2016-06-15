var esservice = require("./elasticsearch.js");
var config = require("./config.js");

exports.index = function(req, res) {
	esservice.index(config.config.labels, req, res);
}