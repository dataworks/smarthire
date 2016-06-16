var esservice = require("./elasticsearch.js");
var config = require("./config.js");

//lists applicants based on type
exports.listApplicants = function(req, res, type) {
	var query = '*';
	if (type !== 'applicant') {
		query = 'type: ' + type;
	}

	esservice.query(config.config.labels, null, res, query, function(res, hits){
    	//var ids = map source -> _id
	    var labelQuery = esservice.map(res, hits, type);
	   	esservice.query(config.config.applicants, req, res, labelQuery, null);
	}, function (error, response) {
        console.log(error);
	});
}