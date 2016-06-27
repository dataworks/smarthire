var elasticsearch = require('elasticsearch');

/**
 * Queries ES with specied query string
 *
 * @param config - object has properties which contain index info
 * @param params - params is passed as req.query
 * @param res - the response object
 * @param query - the query string
 * @param handler - call back function 
 */
exports.query = function(config, params, res, query, handler) {
  var client = new elasticsearch.Client({
    host: config.url
  });

  var sort = {};
  if (params && params.sort) {
    sort[params.sort] = {"order" : (params ? params.order : null), "ignore_unmapped" : true}
  }

  // Execute ES Query
  client.search({
    index: config.index,
    type: config.type,
    from: params ? params.from : null,
    size: params ? params.size : null,
    body: {
      sort: sort ? [sort] : null,
      query: {
        query_string: {
          query: query
        }
      }
    }
  }).then(function(resp) {
    // Parse ES response and send result back
    var hits = module.exports.parseSearchHits(resp, res);

    if (handler) {
      handler(res, hits);
    } else {
      module.exports.defaultHandler(res, hits, resp.hits.total, true);
    }

    // Release client resources
    client.close();
  }, function(err) {
      errorMessage(err, res)
  });
};

/**
 * Parses an ES response, formats it and sends data on the HTTP response.
 *
 * @param resp - HTTP response object from queries
 * @param res - HTTP response
 */
exports.parseSearchHits = function(resp, res) {
  return resp.hits.hits.map(function(hit) {
    return hit._source;
  });
}

/**
 * sends a JSON formatted object of hits
 *
 * @param res - HTTP response from queries
 * @param hits - Data in array format
 */
exports.defaultHandler = function(res, hits, count, useCount) {
  if(useCount)
    res.json({"rows": hits, "size": count});
  else
    res.json(hits);
}

/**
 * Returns aggregated data based on search query for autocomplete functionality
 *
 * @param config - object that contains index, url, and type of ES
 * @param params - params contains the search term
 * @param field - field is set to additionalInfo.resume
 * @param res - HTTP response to send back data
 */
exports.suggest = function(config, term, field, res) {
  var client = new elasticsearch.Client({
    host: config.url
  });

  client.search({
    index: config.index,
    type: config.type,
    body: {
      size: 0,
      aggs: {
        autocomplete: {
          terms: {
            size: 5,
            field: field,
            include: term + ".*",
            order: {
              _count: "desc"
            }
          }
        }
      },
      query: {
        query_string: {
          query: term + "*"
        }
      }
    }
  }).then(function(resp) {
    var hits = module.exports.getSuggestions(resp);
    module.exports.defaultHandler(res, hits, resp.hits.total, false);
    client.close();
  }, function(err) {
      errorMessage(err, res);
  });
}

/*
 * Returns the keys (suggestions) associated with each bucket
 *
 * @param resp - HTTP response from suggest after a search is done
 */
exports.getSuggestions = function(resp) {
  return resp.aggregations.autocomplete.buckets.map(function(hit) {
    return hit.key;
  });
}


/**
 * Indexes data in ES
 *
 * @param config - object that contains indexing info (labels or uploads only)
 * @param params - associated with req.query
 * @param res - HTTP response object to send back data 
 */
exports.index = function(config, params, res) {
  var client = new elasticsearch.Client({
    host: config.url
  });

  client.index({
    index: config.index,
    type: config.type,
    id: params.id,
    body: {
      id: params.id,
      type: params.type,
      base64string: params.base64string ? params.base64string : null
    },
    refresh: true
  }).then(function(response) {
    client.close();
    res.end();
  }, function(err) {
      errorMessage(err, res);
  });
}

/**
 * Deletes an in index in ES
 *
 * @param config - object that contains info about the index being deleted
 * @param params - params maps to req.body
 * @param res - HTTP response object
 */
exports.delete = function(config, params, res) {
  var client = new elasticsearch.Client({
    host: config.url
  });

  client.delete({
    index: config.index,
    type: config.type,
    id: params.id
  }).then(function(response) {
    client.close();
    res.end();
  }, function(err) {
      errorMessage(err, res);
  });
}

/** Logs any errors after CRUD operations
 *
 * @param err - the error
 * @param res - HTTP response object
 */
function errorMessage(err, res) {
  console.log(err.message);
  res.status(500).send(err.message);

  // Release client resources
  client.close();
  res.end();
}