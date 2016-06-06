#!/bin/bash

java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.etl.ResumeReader "$@" --directory ~/Documents/ --master local[*]
