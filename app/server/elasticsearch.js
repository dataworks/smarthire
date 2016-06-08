var elasticsearch = require('elasticsearch');

exports.query = function(config, req, res, query, sort) {
   var client = new elasticsearch.Client({
       host: config.url
   });

   // Execute ES Query
   client.search({
       index: config.index,
       type: config.type,
       body: {
           query: {
               query_string: {
                   query: query
               }
           }
       },
       size: 100,
       sort: sort
   }).then(function(resp) {
       // Parse ES response and send result back
       parseResponse(resp, res);

       // Release client resources
       client.close();
   }, function(err) {
       res.status(400).send(err.message);

       // Release client resources
       client.close();
   });
};

/**
* Parses an ES response, formats it and sends data on the HTTP response.
*/
function parseResponse(resp, res) {
   var hits = resp.hits.hits;
   var results = [];
   for (var x = 0; x < hits.length; x++) {
       results.push(hits[x]._source);
   }

   // Write response direct back to client
   res.send({
       "rows": results,
       "total": results.length
   });
}