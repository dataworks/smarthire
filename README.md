# SmartHire

SmartHire is an open source applicant prioritization system powered by machine learning. The system was created to help organizations go through large backlogs of applicants and find the best candidates for their open positions. The system learns who the best candidates are based on who has previously made it through the hiring process. SmartHire is built on Node.js, AngularJS, Elasticsearch, Apache Spark, Apache Tika, Apache OpenNLP and Scala.

## Using SmartHire

### Initial Installation

SmartHire is powered by a number of dependencies. Npm and sbt help with managing most of these, but some preliminary setup is required. Instructions are provided below:

#### Node.js

SmartHire uses Node as its web server. The download page for Node can be found [here](https://nodejs.org/en/download/). For instructions on installing node via a package manager, see [here](https://nodejs.org/en/download/package-manager/).

Npm (node package manager) is used to install the dependencies for the server. The newer releases of Node come with npm, so be sure to check that it is included.

#### Elasticsearch

Elasticsearch is used to store and quickly serve resume data. Install instructions can be found on the Elasticsearch website [here](https://www.elastic.co/downloads/elasticsearch).

#### Sbt

Sbt, or scala build tool, is required for downloading the dependencies of the ETL processes. Its download page lives [here](http://www.scala-sbt.org/download.html).

#### Spark

Spark is a library that allows SmartHire to scale to any number of resumes. It is located [here](https://spark.apache.org/downloads.html). It is recommended to pick up spark version 1.6.1.

#### Environment Variables

If any of these programs are installed manually, make sure to set up the proper environment variables in a shell startup script. For a user who installs in the home folder, configuration may look something like this...

```
export SPARK_HOME=/home/*USER-NAME*/spark-1.6.1-bin-hadoop2.6
export PATH=$PATH:/home/*USER-NAME*/elasticsearch-2.3.3/bin
export PATH=$PATH:$SPARK_HOME/bin
```

### Starting the Application

Now that the arduous installation task is over, it is time to begin firing everything up. This will be accomplished in two main steps. The first is to set up all of the required servers. Details on how this can be done are located in the app directory [README](https://github.com/dataworks/internship-2016/tree/master/app/README.md). The second step is to load data for the app to use. This process is described in the etl directory [README](https://github.com/dataworks/internship-2016/tree/master/etl/README.md).

## About
SmartHire was built by the Data Works 2016 summer internship team in response to ... the hiring of the Data Works 2016 summer internship team. While designed primarily for the task of filtering future Data Works interns, it is hoped that the application is extensible enough to be useful to others as well.

### The Team

Alissa Cobb  
Brantley Gilbert  
Dennis Huang  
Nitin Sudini  
William Goubeaux

## Technologies Used

### Apache Spark

#### What it was used for:

[Apache Spark](http://spark.apache.org/) served as our distributed computing platform.  It alowed us to process resumes and extract data from them using several machines (called "nodes") rather than simply use a single, expensive machine requiring lots of memory and CPU.  Spark also comes with the machine learning library, [Spark MlLib](http://spark.apache.org/docs/latest/mllib-guide.html), that allowed us to incorporate several machine learing techniques into our applicant scoring algorithm, including logistic regression, naive Bayes, and term frequency–inverse document frequency.

#### Pros:

Spark is relatively high-level and less verbose compared to Hadoop MapReduce.  Primarily written in [Scala](http://www.scala-lang.org/), a JVM language that combines functional and object-oriented programming techniques, but also supports Java and Python.

#### Cons:

Often ran into issues with serialization, which is more of a general parallel-computing issue than a Spark issue.  Ran into some issues with different versions of different libraries being incompatible, such as the [Hadoop-AWS library being incompatible with Spark in more recent versions](https://issues.apache.org/jira/browse/HADOOP-12420).


