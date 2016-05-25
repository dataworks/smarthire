#!/bin/bash

java -cp `cat .classpath` -Xmx2048m applicant.etl.DownloadResumes "$@ -o data/resumes/pdf"
