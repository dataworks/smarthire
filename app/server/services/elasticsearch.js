var elasticsearch = require('elasticsearch');

exports.query = function(config, req, res, query, handler) {
  var client = new elasticsearch.Client({
    host: config.url
  });
  // Execute ES Query
  client.search({
    index: config.index,
    type: config.type,
    from: req ? req.query.from : null,
    size: req ? req.query.size : null,
    body: {
      sort: [
        {
          "score" :{"order" : "asc", "ignore_unmapped" : true}
        }
      ], 
      query: {
        query_string: {
          query: query,
        }
      }
    }
  }).then(function(resp) {
    // Parse ES response and send result back
    var hits = module.exports.parseResponse(resp, res);

    if (handler) {
      handler(res, hits);
    } else {
      module.exports.defaultHandler(res, hits);
    }

    // Release client resources
    client.close();
  }, function(err) {
      errorMessage(err, res)
  });
};

/**
 * Parses an ES response, formats it and sends data on the HTTP response.
 */
exports.parseResponse = function(resp, res) {
  return resp.hits.hits.map(function(hit) {
    return hit._source;
  });
}

exports.defaultHandler = function(res, hits) {
  res.json(hits);
}

/**
 * Creates a new index
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
    },
    refresh: true
  }).then(function(response) {
    client.close();
    res.end();
  }, function(err) {
      errorMessage(err, res);
  });
}

//allows item to be deleted from index
exports.delete = function(config, req, res) {
  var client = new elasticsearch.Client({
    host: config.url
  });

  client.delete({
    index: config.index,
    type: config.type,
    id: req.params.id
  }).then(function(response) {
    client.close();
    res.end();
  }, function(err) {
      errorMessage(err, res);
  });
}

function errorMessage(err, res) {
  console.log(err.message);
  res.status(400).send(err.message);

  // Release client resources
  client.close();
  res.end();
}