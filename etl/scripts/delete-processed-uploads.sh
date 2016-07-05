#!/bin/bash

#script to delete processed resumes in uploads index

curl -XDELETE 'http://interns.dataworks-inc.com/elasticsearch/uploads/upload/_query' -d '
{
  "query": {
    "term": { "processed" : true }
  }
}
'

# Can also be contained in URL:
# curl -XDELETE 'http://interns.dataworks-inc.com/elasticsearch/uploads/upload/_query?q=processed:true'
