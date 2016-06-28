#!/bin/bash

#script to create elasticsearch index with desired mappings

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
