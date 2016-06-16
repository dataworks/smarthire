var esservice = require("./elasticsearch.js");
var config = require("./config.js");

//lists applicants based on type
exports.listApplicants = function(req, res, type) {
	var query = '*';
	if(type !== 'applicant')
		query = 'type: ' + type;

	console.log(query);

	esservice.query(config.config.labels, req, res, query, function(res, hits){
    	//var ids = map source -> _id
	    query = esservice.map(res, hits, type);
	   	esservice.query(config.config.applicants, req, res, query, null);
	}, function (error, response) {
        console.log(error);
	});
}