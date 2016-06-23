var esservice = require("./elasticsearch.js");
var config = require("./config.js");

exports.suggest = function(req, res) {
	esservice.suggest(config.applicants, req.query, res);
}