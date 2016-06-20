var esservice = require("./elasticsearch.js");
var config = require("./config.js");


exports.queryAttachments = function(req, res) {
	console.log(req);
	getAttachment(res, req.query.id, req.query.type);
}

function getAttachment(res, id, type) {
	var q1 = "id:(" + id + ")";
	esservice.query(config.applicants, null, res, q1, function(res, hits) {
		if (hits.length > 0) {
			var source = hits[0];
			var buffer = new Buffer(source.base64string, 'base64');
			res.set({
				"Content-Length": buffer.length,
				"Content-Type": source.metadata[type],
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