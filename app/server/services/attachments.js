var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/**
 * Retrieves images and resumes of applicants
 *
 * @param req - HTTP request object which contains id and type of applicant
 * @param res - HTTP response object
 */
 exports.getAttachment = function(req, res) {
  var query; 
  if(req.query.type === 'image')
    query = "applicantid:" + req.query.id + " AND metadata.Content-Type:" + req.query.type;
  else {
    query = "applicantid:" + req.query.id + " NOT metadata.Content-Type:image";
  }

  esservice.query(config.attachments, null, res, query, function(res, hits) {
    if (hits.length > 0) {
      var source = hits[0];
      var buffer = new Buffer(source.base64string, 'base64');
      res.set({
        "Content-Length": buffer.length,
        "Content-Type": source.metadata['Content-Type'],
        "Content-Disposition": "inline; filename=" + source.filename
      });
      res.send(buffer);
    } else {
      res.status(204);
    }
    res.end();
  }, function(error, response) {
    console.log(error);
  });
}
