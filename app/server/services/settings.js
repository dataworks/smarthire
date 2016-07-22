var esservice = require("./elasticsearch.js");
var config = require("./config.js");

exports.buildQuery = function(req, res) {
  esservice.query(config.settings, req.query, res, {query_string: { query: "reston"}}, null)
}
