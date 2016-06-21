var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/*
 *lists applicants based on type
 */
exports.listApplicants = function(req, res, type) {
  var query = '*';
  if (type !== 'new') {
    query = 'type: ' + type;
  }

  console.log(req.query);
  if (req.query.query) {// != null || req.query.query.length < 1) {
    console.log("Querying");
    esservice.query(config.applicants, req, res, req.query.query, null);
  }
  else {
    esservice.query(config.labels, {query: {size: 5000}}, res, query, function(res, hits){
      var labelQuery = buildQuery(res, hits, type);
      esservice.query(config.applicants, req, res, labelQuery, null);
    },function (error, response) {
      console.log(error);
    });  
  }

}

/*
 * Builds the query string for ES
 */
function buildQuery(res, hits, type) {
  if (hits && hits.length > 0) {
    var ids = hits.map(function(hit) {
      return hit.id;
    });
    //same query logic * or NOT id ()
    if (ids && ids.length > 0) {
      if (type === 'new') {
        return "NOT id:(" + ids.join(" ") + ")";
      } else if (type === 'favorite' || type === 'archive' || type === 'review') {
        return "id:(" + ids.join(" ") + ")";
      }
    }
  }
  return type === 'new' ? '*' : ' ';
}