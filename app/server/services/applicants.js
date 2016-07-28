var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/*
 * lists applicants based on type
 *
 * @param req - HTTP request object that has the query
 * @param res - HTTP response object
 * @param type - type of applicant can be new, favorite, archive, or review
 */
exports.listApplicants = function(req, res, type) {
  var query = '*';
  if (type !== 'new') {
    query = 'type: ' + type;
  }

  if (req.query.query) { 
    esservice.query(config.applicants, req.query, res, {query_string: { query: req.query.query, default_operator: "AND" }}, function(res, hits, count, aggs) {
      var applicants = hits;
      var labelQuery = buildQuery(res, hits, type, query);
      esservice.query(config.labels, applicants.length, res, labelQuery, function(res, hits) {
        //double for loop inefficient probably should switch to hashmap
        for(var x in applicants) {
          for(var y in hits) {
            if(applicants[x].id === hits[y].id) {
              applicants[x].type = hits[y].type;
              break;
            }
          }
          if(!applicants[x].type)
            applicants[x].type = 'new';
        } 
        esservice.defaultHandler(res, applicants, count, true, aggs);
      });
    }, buildAggs());
  }
  else {
    esservice.query(config.labels, {size: 5000}, res, {query_string: { query: query, default_operator: "AND" }}, function(res, hits) {
      var labelQuery = buildQuery(res, hits, type);
      esservice.query(config.applicants, req.query, res, labelQuery, null, buildAggs());
    }, function (error, response) {
      console.log(error);
    });  
  }
}

/*
 * Calls the suggest method in ES.js
 *
 * @param term - the search term
 * @param res - HTTP response object 
 */
exports.suggest = function(term, res) {
  esservice.suggest(config.applicants, term,'additionalInfo.resume', res);
}

/*
 * Aggregations for analyis only returns agg data 
 *
 * @param req - HTTP response object 
 * @param res - HTTP response object 
 */
exports.aggregations = function(req, res) {
  esservice.aggregations(config.applicants, {match_all: {}}, res, buildAggs());
}


/*
 * Builds the query string for ES based on type
 *
 * @param res - HTTP response object
 * @param hits - Id's are extracted from hits and mapped to an array
 * @param type - type of the applicant 
 */
function buildQuery(res, hits, type, query) {
  if (hits && hits.length > 0) {
    var ids = hits.map(function(hit) {
      return hit.id;
    });

    //same query logic * or NOT id ()
    if (ids && ids.length > 0) {
      if(query) {
        return {bool: { must: { ids: { values:  ids }}}}
      }
      else if (type === 'new') {
        return {bool: { must_not: { ids: { values:  ids }}}}
      } 
      // creates a query of all of the label types
      else if (type === 'favorite' || type === 'archive' || type === 'review') {
        return {bool: { must: { ids: { values:  ids }}}}
      }
    }
  } 

  //return type === 'new' ? '*' : ' ';
  return { query_string: { query: " ", default_operator: "AND" }}
}

/*
 * returns a common aggregtaion object
 **/
function buildAggs() {
  return {
    languages: {terms:{ size: 5, field: "skills.language"}}, 
    etl: {terms:{ size: 5, field: "skills.etl"}},
    web: {terms:{ size: 5, field: "skills.webapp"}},
    mobile: {terms:{ size: 5, field: "skills.mobile"}},
    db: {terms:{ size: 5, field:"skills.database"}},
    bigdata: {terms:{ size: 5, field: "skills.bigdata"}}
  }
}
