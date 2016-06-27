#!/bin/bash

#script to create elasticsearch index with desired mappings

curl -XPUT 'http://interns.dataworks-inc.com/elasticsearch/applicants/applicant/_mapping' -d '
{
  "applicant": {
    "properties": {
      "name": {
        "type": "string",
        "analyzer": "english",
        "fields": {
          "raw": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      }
    }
  }
}
'
