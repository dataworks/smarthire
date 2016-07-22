var esservice = require("./elasticsearch.js");
var config = require("./config.js");

exports.buildSetting = function(req, res) {
  esservice.query(config.settings, req.query, res, {query_string: { query: "reston"}}, null)
}
