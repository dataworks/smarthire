#!/bin/bash

#script to create elasticsearch index with desired mappings
curl -XPUT 'http://interns.dataworks-inc.com/elasticsearch/attachments/' -d '
{
  "settings": {
    "index" : {
        "number_of_shards" : 5,
        "number_of_replicas" : 0
    }
  }
}
'

curl -XPUT 'http://interns.dataworks-inc.com/elasticsearch/attachments/attachment/_mapping' -d '
{
  "attachment": {
    "properties": {
      "base64string": {
        "type": "string",
        "index": "no"
      }
    }
  }
}
'
