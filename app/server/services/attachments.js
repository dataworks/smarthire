var esservice = require("./elasticsearch.js");
var config = require("./config.js");

exports.getAttachment = function(req, res) {
	var query = "applicantid:" + req.query.id + " AND metadata.Content-Type:" + req.query.type;
	esservice.query(config.attachments, null, res, query, function(res, hits) {
		if (hits.length > 0) {
			var source = hits[0];
			var buffer = new Buffer(source.base64string, 'base64');
			res.set({
				"Content-Length": buffer.length,
				"Content-Type": source.metadata['Content-Type'],
				"Content-Disposition": "attachment; filename=" + source.filename
			});
			res.send(buffer);
		}
		else {
			res.status(404);
		}
	},function (error, response) {
    	console.log(error);
		});
}
