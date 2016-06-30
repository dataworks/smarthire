#!/bin/bash
java -cp `cat .classpath` -Xmx2048m -Dlog4j.configuration=file:log4j.properties applicant.ml.regression.MlModelGenerator "$@" --word2vecModel model/w2v --master local[1] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --labelindex labels --logisticmodeldirectory model/regression/ 
