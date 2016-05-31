#!/bin/bash

java -cp `cat .run` -Xmx2048m applicant.etl.DownloadResumes "$@ -o data/resumes/pdf"
