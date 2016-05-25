#!/bin/bash

java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.ml.rnn.TextNetTest -j model/rnn/512.json -c model/rnn/512.bin
