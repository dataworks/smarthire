
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

Elasticsearch was used to store all of the data that we used for this project.  You can create different indexes or indices withing Elasticsearch, which we did to store the different types of data that we collected.  

#### Pros:

* Easy for the UI side to pull data from to display neatly on the webpage
* bar

#### Cons:

* foo
* bar

---

### [Bootstrap](http://getbootstrap.com/getting-started/)

#### What it was used for:

Bootstrap is a framework that we used to help create the structure for the project.  The nice thing about bootstrap is that it has a ton of piece and parts that are provided within the frame work that help ease the prosess of building any webpage.
#### Pros:

* There is a ton of easy to use libraries. 
* Provides templates to help start projects.
* Well documented.
* Easy to make a template your own.

#### Cons:

* Small section of icons that can be used.
* Doesn't work with Angular Material.


---

### [AngularJS](https://angularjs.org/)
#### What it was used for:

ljsdfadjfadjfaldfj
#### Pros:

* bar
* foo

#### Cons:

* bar
* foo

---

### [Node.js](https://nodejs.org/en/)
#### What it was used for:

alsdfjadslfjadlfkj
#### Pros:

* bar
* foo


#### Cons:

* bar
* foo

---

### [Express.js](https://expressjs.com/en/starter/installing.html)
#### What it was used for:

asdfadfadsfadsfadsfadfasdfad
#### Pros:

* bar
* foo

#### Cons:

* bar
* foo

---

### [Charts.js](http://www.chartjs.org/docs/)
#### What it was used for:

ad;fasdfasdfasdf
#### Pros:

* bar 
* foo

#### Cons:

* bar
* foo


