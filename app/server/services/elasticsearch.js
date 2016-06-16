var elasticsearch = require('elasticsearch');

exports.query = function(config, req, res, query, handler) {
   var client = new elasticsearch.Client({
       host: config.url
   });
console.log(req.query)
// Execute ES Query
  client.search({
      index: config.index,
      type: config.type,
      from: req.query.from,
      size: req.query.size,
      body: {
          query: {
              query_string: {
                  query: query
              }
          }
      }
   // // Execute ES Query
   // client.search({
   //     index: config.index,
   //     type: config.type,
   //     from: req.query.from,
   //     size: req.query.size,
   //     body: {
   //         query: {
   //             query_string: {
   //                 query: query
   //             }
   //         }
   //     }
       // sort: sort 
   }).then(function(resp) {
       // Parse ES response and send result back
       var hits = module.exports.parseResponse(resp, res);

       if (handler) {
          handler(res, hits);
       }
       else {
          module.exports.defaultHandler(res, hits);
       }

       // Release client resources
       client.close();
   }, function(err) {
       console.log(err.message);
       res.status(400).send(err.message);

       // Release client resources
       client.close();
   });
};

/**
* Parses an ES response, formats it and sends data on the HTTP response.
*/
exports.parseResponse = function(resp, res) {
    return resp.hits.hits.map(function(hit) { return hit._source; });
}

exports.defaultHandler = function(res, hits) {
    res.json(hits);
}

/**
* Creates a new index
*
*/
exports.index = function(config, req, res) {
  var client = new elasticsearch.Client({
    host: config.url
  });

  var id = req.body.id;
  var type = req.body.type;

  client.index({
    index: config.index,
    type: config.type,
    id: id,
    body: {
      id: id,
      type: type,
    },
    refresh: true
  }).then(function (error, response) {
    console.log(error);
    res.end();
  });
}

/**
/* returns a query string for ES
*/
exports.map = function(res, hits, type) {
    var query = '*';
    if (hits && hits.length > 0) {
      var ids = hits.map(function(hit) { return hit.id; });
      //same query logic * or NOT id ()
      if (ids && ids.length > 0) { 
        if(type === 'applicant') {
          query = "NOT id:(" + ids.join(" ") + ")";
        }
        else if(type === 'favorite' || type === 'archive') {
          query = "id:(" + ids.join(" ") + ")";
        }
      }
    }
    return query; 
}