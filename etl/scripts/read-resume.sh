#!/bin/bash

java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.etl.ResumeReader "$@" --directory ~/Documents/ --master local[*] --nodes 172.31.61.189 --port 9200
