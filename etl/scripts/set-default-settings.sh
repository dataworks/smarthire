#!/bin/bash
java -cp `cat .classpath` -Xmx4096m -Dlog4j.configuration=file:log4j.properties applicant.ml.regression.DefaultSettingsSetter "$@" --master local[2] --nodes 172.31.61.189 --port 9200 --settingsindex mlsettings
