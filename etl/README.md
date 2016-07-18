# SmartHire ETL, Machine Learning and Analytics

Scala and Apache Spark jobs to ETL data into required formats to support the front-end web application. Machine learning and Analytics jobs to support prioritization and ranking of data.

## ETL Setup

Note: It is assumed that all necessary dependencies have been installed as described in the main [README](https://github.com/dataworks/internship-2016/blob/master/README.md) and that the servers have been configured as described in the app [README](https://github.com/dataworks/internship-2016/blob/master/app/README.md).

### Building the Programs

Sbt is used to get ETL dependencies and compile the scala source code. Navigate to the etl folder and execute:

```
sbt compile
```

This should give of the necessary jars.

### Loading the Data

#### About the scripts

The code in the ETL folder contains a number of source files that deal with different sections of the data. All of these programs can be interacted with by using the scripts within the [scripts folder](https://github.com/dataworks/internship-2016/tree/master/etl/scripts), but in order to use these, a .classpath file is required. Create this file in the etl folder with sbt by running:

```
sbt 'export fullClasspath' > .classpath
```

Then edit this file with a text editor, and delete the top line until only a list of paths remains. If all goes well, the .classpath file should look something like this:

```
/home/*USER-NAME*/internship-2016/etl/target/scala-2.10/classes:/home/*USER-NAME*/spark-1.6.1-bin-hadoop2.6/lib/spark-assembly-1.6.1-hadoop2.6.0.jar: ...
```

Everything should now be properly set up to do some ETL. The relevant scripts are described below, but as a word of caution, these scripts were set up for Data Work's specific use case. Some configuration is required to change things such as the Elasticsearch IP. In order to help facilitate these changes, scripts with command line options all have a `--help` option that describes what the others do.

#### Creating proper Elasticsearch mapings

The creation and mapping of the applicants and attachments index have been automated via the `create-elasticsearch-mappings.sh` and `create-elasticsearch-attachments-mappings.sh` scripts. It is also important to create a labels and uploads index via `curl` or a front end like [elasticsearch-head](https://github.com/mobz/elasticsearch-head).

#### Building OpenNLP models

OpenNLP requires trained data models in order to function correctly. These models require a training file with highlighted entities. Such a training file may be created with the `download-resumes.sh` script in conjunction with the `build-nlp-data.sh` script. Once the training file is made, the NLP models can be created with`nlp-model-train.sh`.

#### Putting extracted data into Elasticsearch

There are a number of ways that resumes can be uploaded.

1. If resumes are sitting on a normal file system, they can be uploaded with `read-resume.sh`. Be sure to edit the directory of the resumes.


2. If resumes reside within an s3 bucket, they can be extracted with the `read-from-s3.sh` script. This script requires a .keyfile file in the etl directory. The first line of this file should contain the amazon s3 access key and the second line should contain the s3 secret access key. 


3. Resumes can be read right from Elasticsearch after being uploaded from the web app interface via `upload-resume.sh`. In order to clear out uploads from the uploads index that have already been read, use `delete-processed-uploads.sh`.

Uploading profile pictures is done after the resumes. Use `extract-pictures.sh` and the pictures will appear in the web app.

#### Building models for applicant scores

At this point, the app should be at a useable state. The final step is to give applicants their scores. The machine learning score calculation is unique to each user and uses the favorited and archived index in order to learn which resumes are important. Because of this, the first step is to manually favorite and archive applicants. Since these applicants are used as training data, the more of them that are categorized, the better, but having around 100 each in favorites and archives should be enough to start seeing accurate results. Once ready, run `build-ml-models.sh` to generate the score models.

#### Updating applicant scores

Updating scores is simple. Once the score models are created, use `update-scores.sh` and the new scores will be calculated and put into Elasticsearch for the web app to display.
