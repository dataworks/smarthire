var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/**
 * Indexes applicants under favorites, archive, or review
 *
 * @param req - HTTP request object with relevant data in the body
 * @param res - HTTP response object
 */
exports.index = function(req, res) {
  esservice.index(config.labels, req.body, res);
}

/**
 * Removes an applicant from an index
 *
 * @param req - HTTP request object with relevant data in req.params
 * @param res - HTTP response object
 */
exports.delete = function(req, res) {
  esservice.delete(config.labels, req.params, res);
}