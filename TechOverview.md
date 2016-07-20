
## Technologies Used

### [Apache Spark](http://spark.apache.org/) 

#### What it was used for:

Apache Spark served as our distributed computing platform.  It allowed us to process resumes and extract data from them using several machines (called "nodes") rather than simply use a single, expensive machine requiring lots of memory and CPU.  Spark also comes with the machine learning library, [Spark MlLib](http://spark.apache.org/docs/latest/mllib-guide.html), that allowed us to incorporate several machine learing techniques into our applicant scoring algorithm, including logistic regression, naive Bayes, and term frequency–inverse document frequency.

#### Pros:

* Spark is relatively high-level and less verbose compared to Hadoop MapReduce.  
* Primarily written in [Scala](http://www.scala-lang.org/), a JVM language that combines functional and object-oriented programming techniques, but Spark also supports Java and Python.
* Well integrated with most of the Hadoop ecosystem as well as Amazon Web Services and Elasticsearch.

#### Cons:

* Often ran into issues with serialization, which is more of a general parallel-computing issue than a Spark issue.
* Ran into some issues with different versions of different libraries being incompatible, such as [the newer versions of the Hadoop-AWS library being incompatible with Spark](https://issues.apache.org/jira/browse/HADOOP-12420).

---

### [Apache Tika with Tesseract OCR](https://tika.apache.org/)

#### What it was used for:

Used to extract text and metadata from the resume files.  Tika is able to parse text from multiple file formats out of the box, including MS Word documents, PDFs, and plain text files.  In order to extract text from images, including those imbedded in PDF files (i.e. scanned PDF resumes), Tika offers support for the optical character recognition (OCR) program [Tesseract](https://github.com/tesseract-ocr/tesseract).

#### Pros:

* Very lightweight, able to extract data from files quickly using just a few lines of code.
* Written in Java, works well in Scala.
* Tesseract highly accurate for open-source OCR system.

#### Cons:

* Opening and closing streams improperly can cause issues with file type detection.
* Documentation can be confusing sometimes, especially for integration with Tesseract.
* Tesseract must be installed on machine (in our case all nodes) and in path variable.

---

### [Apache OpenNLP](https://opennlp.apache.org/)


#### What it was used for:

Apache OpenNLP (Natural Language Processing) was used to tag entities (names, locations, skills, etc.) within the raw resume text returned from Tika.  We used this entity tagging process to create our applicant profiles, which were then uploaded to Elasticsearch.  Using a NLP system such as OpenNLP allowed us to extract usable data from resumes of various formats and structures.

#### Pros:

* High accuracy after training models.
* Supports regular expressions, useful for keyword matching and clearly defined structured entities such as URLs.
* Includes chunker, tokenizer, sentence detector, and part-of-speech tagging.

#### Cons:

* Not thread safe, must pass object to each node within cluster.
* Model binary files are large and take some time to load into JVM.
* Like any other NLP system, it's not entirely accurate, so it often fails to recognize entities such as names.

---

### [Elasticsearch](https://www.elastic.co/products/elasticsearch)

#### What it was used for:
  		  
Elasticsearch is a search engine built on the Apache Lucene tokenizer project.  It was used as our main data storage, holding both our applicant profiles as well as the source PDF files and images encoded as base-64 strings.  The [elasticsearch-head plugin](https://mobz.github.io/elasticsearch-head/) was used to manage the Elasticsearch indices through a browser-based UI.

#### Pros:
  		  
* Universal language support through REST API, including JSON structured queries
* Native Apache Spark support through [Elasticsearch-Hadoop](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html) library
* Works well as a distributed document-store since it is scalable, flexible, and schema-free

#### Cons:
  		  
* Not intended to hold large objects such as image files.
* Indexing adds a lot of overhead, so storage usage can grow quickly.
* No relational structure, so all joins must be done outside database.
* Distributed, so data is not always available immediately after writing to index.

---

### [Bootstrap](http://getbootstrap.com/getting-started/)

#### What it was used for:

Bootstrap is a framework that we used to help create the structure for the project.  The nice thing about Bootstrap is that it has a ton of pieces and parts that are provided within the framework that help ease the prosess of building any webpage.
#### Pros:

* There are a ton of easy to use libraries. 
* Provides templates to help start projects.
* Well documented.
* Easy to make a template your own.

#### Cons:

* Small section of icons that can be used.
* Doesn't work with Angular Material.


---

### [AngularJS](https://angularjs.org/)
#### What it was used for:

AngularJS is a JavaScript based framework that extends upon HTML for more dynamic web applications that follows the [MVC](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) architecture. The framework's ability to bind data and inject dependencies eliminate the need to write a huge chunk of code. It was used to create many of the functionalities seen on the client side, and it also connected the front-end to the server components. 
#### Pros:

* Improves server performance by supporting caching 
* Many libraries that can be implemented
* Extends upon HTML base
* Easy to test

#### Cons:

* Hard to debug scopes 
* Lack of configuration if used with Bootstrap
* Steep learning curve after mastering the basics

---

### [Node.js](https://nodejs.org/en/)
#### What it was used for:

Node.js is an open-source runtime environment that is used to develop the server-side of a web application. Many of its modules are written in JavaScript, which makes it ideal to use it with another JavaScript based web application development framework, such as AngularJS. It was used to link Elasticsearch to the application and load up the data in a back-end call for the front-end to grab and display. 
#### Pros:

* Dynamic language (JavaScript) makes it much faster than other server-side environments
* Lots of great libraries that are easy to install with [npm](https://www.npmjs.com/)
* Scalable to thousands of active connections


#### Cons:

* Nested callbacks make it hard to go through and debug
* Not suited for CPU intensive tasks
* Hard to use with a relational database system

---

### [Express.js](https://expressjs.com/en/starter/installing.html)
#### What it was used for:

Express.js is a Node.js framework that helps organize a web application's routing and templating in a MVC format. Express.js does this so that minimal effort is needed, and it is very simple to use. It was used to make sure that code was not repeated unnecessarily on the server side. 
#### Pros:

* Saves a lot of time and eliminates need to write a lot of code for server routing
* Extends Node.js capability for web development
* Allows generation of complex apps quickly

#### Cons:

* Not many well written middlewares for usage
* JavaScript code bases are harder to maintain than other languages

---

### [Charts.js](http://www.chartjs.org/docs/)
#### What it was used for:

Charts.js is an open-source library that creates HTML5 charts and graphs for a webpage. The charts are based off JavaScript, and they are easily modified to suit a user's liking. This library was used to create the pie charts seen in Analytics and in the carousel on the home page. 
#### Pros:

* Based off JavaScript, easy to integrate with AngularJS 
* Visually appealing, graphs are also interactive

#### Cons:

* Harder to work with Elasticsearch data, Kibana may have been easier
