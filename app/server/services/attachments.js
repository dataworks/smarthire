var esservice = require("./elasticsearch.js");
var config = require("./config.js");

/**
 * Retrieves images of applicants
 *
 * @param req - HTTP request object which contains id and type of applicant
 * @param res - HTTP response object
 */
exports.getAttachment = function(req, res) {
  var query = "applicantid:" + req.query.id + " AND metadata.Content-Type:" + req.query.type;

  esservice.query(config.attachments, null, res, query, function(res, hits) {
    if (hits.length > 0) {
      sendBuffer(hits[0], res);
    } else {
      res.status(204);
    }
    res.end();
  }, function(error, response) {
    console.log(error);
  });
}

/**
 * Retrieves resumes of applicants
 *
 * @param req - HTTP request object which contains id of applicant
 * @param res - HTTP response object
 */
exports.getResume = function(req, res) {
  var query = "applicantid:" + req.query.id;
  var extensions = ["pdf", "doc", "docx", "txt"];

  esservice.query(config.attachments, null, res, query, function(res, hits) {
    if (hits.length > 0) {
      for (var i = 0; i < hits.length; i++) {
        if (extensions.indexOf(hits[i].extension) !== -1) {
          sendBuffer(hits[i], res);
          break;
        }
      }
    } else {
      res.status(204);
    }
    res.end();
  }, function(error, response) {
    console.log(error);
  });
}

/**
 * Converts base64string to source file
 *
 * @param source - source field of index
 * @param res - HTTP response
 */
function sendBuffer(source, res) {
  var buffer = new Buffer(source.base64string, 'base64');
  res.set({
    "Content-Length": buffer.length,
    "Content-Type": source.metadata['Content-Type'],
    "Content-Disposition": "inline; filename=" + source.filename
  });
  res.send(buffer);
}