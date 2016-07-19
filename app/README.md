# SmartHire Web Application

The SmartHire web app is a Node.js application to allow search and display of data. Setting up the server requires configuring Node.js to be able to connect to Elasticsearch and installing the required packages. 

## Server Set Up

### Linking an Elasticsearch index

Npm makes it easy to install all required dependencies. Simply navigate to the app folder,  ```/your-directory/app/```, and execute:

```
npm install
```
SmartHire pulls data from Elasticsearch to display it. Instructions on creating an Elasticsearch index can be found in the ETL [README](https://github.com/dataworks/internship-2016/tree/master/etl/README.md). 

Once an Elasticsearch index has been created, it can be linked to Node.js in config.js. This file can be found here: ```/your-directory/app/server/services```. In config.js, the variable ```host``` is set to the Elasticsearch index.

```
var host = "your-elasticsearch-index.com";
```

### Setting up SSL
Although SSL is not completely necessary to run SmartHire, it is highly recommended, because the nature of this app is dealing with information that should not be public. To set up SSL, a key and certificate are required. These can be obtained by following the tutorial [here](https://www.sitepoint.com/how-to-use-ssltls-with-node-js/) (scroll down about halfway to **Generating Certificates**).

Once the key and certificate have been obtained, navigate to ```/your-directory/server/``` and place the two files (server.key and server.crt) there. They should be in the same place as server.js. The code in server.js and config.js will handle the rest of setting up SSL, so no other steps are necessary. 


## Running the App Locally

In order to start the Node server, navigate back to the app folder   enter the command:

```
npm start
```

Once the command has been executed, the terminal should show something similar to this:

```
> your-directory-app@1.0.0 start /your-directory/app
> node server/server.js

SmartHire listening at https://:::8082
```

Now simply open a web browser and navigate to:

```
https://localhost:8082/app/applicants
```

If the ETL side has been configured, then the web app will be rendered and ready to go! Otherwise, see the ETL [README](https://github.com/dataworks/internship-2016/tree/master/etl/README.md), because the web application will throw errors. 

***Note:*** When navigating to localhost for the first time, an untrusted webpage error may be encountered. This is expected as the certificate was generated on a local machine and was not from an authorized entity (i.e. GoDaddy). Select the *continue to web page* option to go to the app. 

## Continuous Integration

SmartHire was deployed to a continuous integration service using [Jenkins](https://jenkins.io/). This web app can easily be deployed using different technologies.  

Jenkins continually runs tests after code has been pushed to Github. The test file that has already been written can be found in ```/your-directory/test/server```. The frameworks used to write these unit tests are [Mocha](https://mochajs.org/) and [Chai](http://chaijs.com/). The URLs located in the test server.js file will need to be altered to match the server that SmartHire is running off of.

The tests can easily be modified to suit different needs. See [Chai's API](http://chaijs.com/api/) for different unit tests that can be written. 

Once the app has been successfully deployed, open up a web browser and enter the URL where SmartHire is being hosted: 
```
https://your-host-site.com/app/applicants
```

The app will then be loaded on a dedicated server. 
