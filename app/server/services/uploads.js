var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/*
 * Indexes user uploaded attachments
 *
 * @param req - HTTP request object with relevant data in the body
 * @param res - HTTP response object
 */ 
exports.index = function(req, res) {
  esservice.index(config.uploads, req.body, res);
}