var config = {};
var host = "interns.dataworks-inc.com/elasticsearch";

config.labels = {
	url: host,
	index: "labels",
	type: "label"
}

config.applicants = {
	url: host,
	index: "applicants",
	type: "applicant"
}

exports.config = config;