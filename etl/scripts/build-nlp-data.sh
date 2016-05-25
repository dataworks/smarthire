#!/bin/bash

java -cp `cat .classpath` -Xmx2048m applicant.etl.MergeResumes -o model/train/resumes-nlp.train -f nlp -r data/resumes/txt -a data/attributes -c 30000
