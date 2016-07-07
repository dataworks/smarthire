#!/bin/bash

(
 # Wait for lock on /var/lock/.myscript.exclusivelock (fd 200) for 10 seconds
 flock -x -w 10 200 || exit 1

 java -cp `cat .classpath` -Xmx3072m -Dlog4j.configuration=file:log4j.properties applicant.etl.ResumeParser "$@" --directory data/resumes/pdf/ --master local[*] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --attachmentindex attachments --fromElasticsearch --uploadindex uploads --regex model/nlp/regex.txt --models model/nlp/en-ner-degree.bin,model/nlp/en-ner-location.bin,model/nlp/en-ner-organization.bin,model/nlp/en-ner-person.bin,model/nlp/en-ner-school.bin,model/nlp/en-ner-title.bin
 java -cp `cat .classpath` -Xmx3072m -Dlog4j.configuration=file:log4j.properties applicant.ml.score.ScoreCalculator "$@" --word2vecModel model/w2v --master local[*] --nodes 172.31.61.189 --port 9200 --applicantindex applicants --regressionmodeldirectory model/regression/ --naivebayesmodeldirectory model/bayes
 java -cp `cat .classpath` -Xmx3072m -Dlog4j.configuration=file:log4j.properties applicant.etl.PictureExtractor "$@" --master local[1] --nodes 172.31.61.189 --port 9200 --attachmentindex attachments --github

) 200>/var/lock/.myscript.exclusivelock
