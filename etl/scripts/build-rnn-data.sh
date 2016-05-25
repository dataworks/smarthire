#!/bin/bash

java -cp `cat .classpath` -Xmx2048m applicant.etl.MergeResumes -o model/train/resumes-rnn.train -f rnn -r data/resumes/txt
