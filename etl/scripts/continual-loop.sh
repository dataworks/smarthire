#!/bin/bash
while true
do
  java -cp `cat .classpath` -Xmx6144m -Dlog4j.configuration=file:log4j.properties applicant.etl.ResumeParser "$@" --directory data/resumes/pdf/ --master local[*] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --attachmentindex attachments --fromSource 2 --accesskey .keyfile --uploadindex uploads --regex model/nlp/regex.txt --models model/nlp/en-ner-degree.bin,model/nlp/en-ner-location.bin,model/nlp/en-ner-organization.bin,model/nlp/en-ner-person.bin,model/nlp/en-ner-school.bin,model/nlp/en-ner-title.bin
  sleep 1
  #java -cp `cat .classpath` -Xmx6144m -Dlog4j.configuration=file:log4j.properties applicant.ml.regression.MlModelGenerator "$@" --word2vecModel model/w2v --master local[1] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --labelindex labels --logisticmodeldirectory model/regression/ --naivebayesmodeldirectory model/bayes/
  #sleep 1
  java -cp `cat .classpath` -Xmx6144m -Dlog4j.configuration=file:log4j.properties applicant.ml.score.ScoreCalculator "$@" --idfmodeldirectory model/idf --word2vecModel model/w2v --master local[*] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --regressionmodeldirectory model/regression/ --naivebayesmodeldirectory model/bayes
  sleep 1
  java -cp `cat .classpath` -Xmx6144m -Dlog4j.configuration=file:log4j.properties applicant.etl.PictureExtractor "$@" --master local[1] --nodes 172.31.61.189 --port 9200 --attachmentindex attachments --github
  sleep 1
done
