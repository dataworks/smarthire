var elasticsearch = require('elasticsearch');

exports.query = function(config, req, res, query, handler) {
   var client = new elasticsearch.Client({
       host: config.url
   });

   // Execute ES Query
   client.search({
       index: config.index,
       type: config.type,
       body: {
          size: 25,
           query: {
               query_string: {
                   query: query
               }
           }
       }
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