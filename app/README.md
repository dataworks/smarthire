# SmartHire Web Application

The SmartHire web app is a Node.js application to allow search and display of data. Setting up the front-end requires configuring Node.js to be able to connect to Elasticsearch or Amazon S3 and installing the required packages. 

## Server Set Up



Npm makes it easy to install all required dependencies. Simply navigate to the app folder,  ```/your-directory/app/```, and execute:

```
npm install
```
--make note of connecting Node.js to Elasticsearch/S3 here--

Once an Elasticsearch index has been created, it can be linked to Node.js in config.js. This file can be found here: ```/your-directory/app/server/services```. 

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

If the ETL side has been configured, then the web app will be rendered and ready to go! Otherwise, see the other [README](https://github.com/dataworks/internship-2016/tree/master/etl/README.md), because the web application will throw errors. 

## Deploying the App on a Dedicated Server

SmartHire was deployed to a dedicated server using [Jenkins](https://jenkins.io/), and that is what will be covered. This web app can easily be deployed using different technologies.  

