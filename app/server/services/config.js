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

config.attachments = {
  url: host,
  index: "attachments",
  type: "attachment"
}

config.uploads = {
  url: host,
  index: "uploads",
  type: "upload"
}

config.ssl = {
 key  : 'server.key',
 cert : 'server.crt'
};

module.exports = config;