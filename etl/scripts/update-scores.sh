#!/bin/bash

java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.ml.score.ScoreCalculator "$@" --word2vecModel model/w2v --master local[*] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --regressionmodeldirectory model/regression/ --naivebayesmodeldirectory model/bayes --idfmodeldirectory model/idf --everyone
