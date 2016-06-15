var esservice = require("./elasticsearch.js");
var configs = require("./config.js");

exports.index = function(req, res) {
	esservice.index(config.laels, req, res);
}