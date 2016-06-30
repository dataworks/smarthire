#!/bin/bash

#script to create elasticsearch index with desired mappings

curl -XPUT 'http://interns.dataworks-inc.com/elasticsearch/applicants/' -d '
{
  "settings": {
    "index" : {
        "number_of_shards" : 5,
        "number_of_replicas" : 0
    },
    "analysis": {
       "analyzer": {
        "skills_analyzer": {
          "type": "custom",
          "tokenizer": "whitespace",
          "filter": ["lowercase", "stop"]
        }
      }
    }
  }
}
'

curl -XPUT 'http://interns.dataworks-inc.com/elasticsearch/applicants/_mapping/applicant/' -d '
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
      },
      "skills": {
        "properties": {
          "bigdata": {
            "type": "string",
            "analyzer": "skills_analyzer"
          },
          "database": {
            "type": "string",
            "analyzer": "skills_analyzer"
          },
          "etl": {
            "type": "string",
            "analyzer": "skills_analyzer"
          },
          "language": {
            "type": "string",
            "analyzer": "skills_analyzer"
          },
          "mobile": {
            "type": "string",
            "analyzer": "skills_analyzer"
          },
          "webapp": {
            "type": "string",
            "analyzer": "skills_analyzer"
          }
        }
      }
    }
  }
}
'
