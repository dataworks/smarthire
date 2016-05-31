#!/bin/bash

COMMAND="java -cp `cat .run` -Xmx2048m applicant.nlp.ModelTrain"

TRAINING=model/resumes-nlp.train

# Build all models in parallel
$COMMAND -i $TRAINING -o model/nlp/en-ner-degree.bin -e degree -c 3 &
$COMMAND -i $TRAINING -o model/nlp/en-ner-location.bin -e location -c 3 &
$COMMAND -i $TRAINING -o model/nlp/en-ner-organization.bin -e organization -c 3 &
$COMMAND -i $TRAINING -o model/nlp/en-ner-person.bin -e person -c 3 &
$COMMAND -i $TRAINING -o model/nlp/en-ner-school.bin -e school -c 3 &
$COMMAND -i $TRAINING -o model/nlp/en-ner-title.bin -e title -c 3 &
