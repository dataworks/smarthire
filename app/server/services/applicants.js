var esservice = require("./elasticsearch.js");
var config = require("./config.js");

//lists applicants based on type
exports.listApplicants = function(req, res, type) {
	esservice.query(config.label, req, res, "*", function(res, hits){
    	//var ids = map source -> _id
    	var query = esservice.map(res, hits, type);
    	esservice.query(configs.applicants, req, res, query, null);
  	}, function (error, response) {
        console.log(error);
	});
}