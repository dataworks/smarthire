var esservice = require("./elasticsearch.js");
var config = require("./config.js");

exports.attachmentIndex = function(req, res) {
	esservice.index(config.config.attachments, req, res);
}