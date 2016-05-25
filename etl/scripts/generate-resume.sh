#!/bin/bash

java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.etl.ResumeGenerator -a data/attributes -j model/rnn/512.json -c model/rnn/512.bin
