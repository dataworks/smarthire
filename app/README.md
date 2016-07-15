# SmartHire Web Application

The SmartHire web app is a Node.js application to allow search and display of data.

## Server Set Up

### Running the App Locally

Npm makes it easy to install all required dependencies. Simply navigate to the app folder and execute:

```
npm install
```

In order to start the Node server,  enter the command:

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

If the ETL side has been configured, then the web app will be rendered and ready to go! If not, see the other [README](https://github.com/dataworks/internship-2016/tree/master/etl/README.md), otherwise the web page will be empty. 

### Deploying the App on a Dedicated Server
