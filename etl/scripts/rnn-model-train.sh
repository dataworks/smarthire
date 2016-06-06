#!/bin/bash

java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.ml.rnn.TextNetTrain -i model/train/resumes-rnn.train -j model/rnn/textnet.json -c model/rnn/textnet.bin
