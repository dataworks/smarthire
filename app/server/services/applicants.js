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
    esservice.query(config.applicants, req.query, res, {query_string: { query: req.query.query, default_operator: "AND" }}, null);
  }
  else {
    esservice.query(config.labels, {size: 5000}, res, {query_string: { query: query, default_operator: "AND" }}, function(res, hits) {
      var labelQuery = buildQuery(res, hits, type);
      esservice.query(config.applicants, req.query, res, labelQuery, null);
    },function (error, response) {
      console.log(error);
    });  
  }
}

/*
 * Builds the query string for ES based on type
 *
 * @param res - HTTP response object
 * @param hits - Id's are extracted from hits and mapped to an array
 * @param type - type of the applicant 
 */
function buildQuery(res, hits, type) {
  if (hits && hits.length > 0) {
    var ids = hits.map(function(hit) {
      return hit.id;
    });

    //same query logic * or NOT id ()
    if (ids && ids.length > 0) {
      if (type === 'new') {
        return {bool: { must_not: { ids: { values:  ids }}}}
        //return "NOT id:(" + ids.join(" OR ") + ")";
      } 
      // creates a query of all of the label types
      else if (type === 'favorite' || type === 'archive' || type === 'review') {
        return {bool: { must: { ids: { values:  ids }}}}
        //return "id:(" + ids.join(" OR ") + ")";
      }
    }
  } 

  //return type === 'new' ? '*' : ' ';
  return { query_string: { query: " ", default_operator: "AND" }}
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
 * Aggregations for analyis
 *
 * @param res - HTTP response object 
 * @param field - ES field
 */
exports.aggregations = function(res, type, field, query) {
  var q = '*';
  if(type !== 'new')
    q = "type:" + type;

  if(type) {
    esservice.query(config.labels, {size: 5000}, res, { query_string: { query: q, default_operator: "AND" }}, function(res, hits) {
      var labelQuery = buildQuery(res, hits, type);
      aggs(field, labelQuery, res);
    },function (error, response) {
      console.log(error);
    });
  } else {
      aggs(field, { query_string: { query: query, default_operator: "AND" }}, res);
  }
}

/**
 * Private function for aggregation queries
 */
function aggs(field, query, res) {
  if(field === 'languages') 
    esservice.aggregations(config.applicants, 'skills.language', query, res);

  if(field === 'etl')
    esservice.aggregations(config.applicants, 'skills.etl', query, res);

  if(field === 'web')
    esservice.aggregations(config.applicants, 'skills.webapp', query, res);

  if(field === 'mobile')
    esservice.aggregations(config.applicants, 'skills.mobile', query, res);

  if(field === 'db')
    esservice.aggregations(config.applicants, 'skills.database', query, res);

  if(field === 'bigData')
    esservice.aggregations(config.applicants, 'skills.bigdata', query, res);
}