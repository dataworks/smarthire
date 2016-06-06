#!/bin/bash

java -cp `cat .classpath` -Xmx2048m applicant.nlp.ModelTest -m model/nlp/en-ner-degree.bin,model/nlp/en-ner-location.bin,model/nlp/en-ner-organization.bin,model/nlp/en-ner-person.bin,model/nlp/en-ner-school.bin,model/nlp/en-ner-title.bin -p model/nlp/regex.txt
