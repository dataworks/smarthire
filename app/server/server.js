var express = require("express");
var bodyParser = require("body-parser");
var elasticsearch = require("elasticsearch");
var root = express();
var app = express();
var fs = require('fs');
var https = require('https');
var passport = require('passport');

var applicantService = require("./services/applicants.js");
var labelService = require("./services/labels.js");
var attachmentService = require("./services/attachments.js");
var uploadService = require("./services/uploads.js");
var options = require("./services/config.js");

//used for serving static files (html, client js, images)
app.use(express.static("client"));

//node module directory
app.use(express.static("node_modules"));

//max file size for uploads
app.use(bodyParser.json({limit: '50mb'}));

//code for login/login authentication
var session = require('express-session');
app.use(session({
  secret: "no",
  resave: false,
  saveUninitialized:false,
  name: "test"
}));
app.use(passport.initialize());
app.use(passport.session());

//parses the text as url encoded data and exposes the resulting object on req.body
app.use(bodyParser.urlencoded({ 
  extended: true
}));

//body-parser extracts the entire body portion of an incoming request stream 
//and exposes it on req.body
app.use(bodyParser.json());

/**
 * HTTP GET request to retrieve applicants
 *
 * @param route - Routes HTTP GET requests to the specified path
 * @param callback function - Calls a service method to get specified applicants
 */
app.get("/service/applicants", function(req, res) {
  var type = null;
  if (!req.query || !req.query.type) {
    type = "new";
  } else {
    type = req.query.type.toLowerCase();
  }
  applicantService.listApplicants(req, res, type);
});

/**
 * HTTP POST request to index applicants
 *
 * @param route - Routes HTTP POST requests to the specified path
 * @param callback function - Calls a service method to index an applicant
 */
app.post("/service/labels", function(req, res) {
  labelService.index(req, res);
});

/**
 * HTTP DELETE request to retrieve applicants
 *
 * @param route - Routes HTTP DELETE requests to the specified path
 * @param callback function - Calls a service method to remove an applicant from labels index
 */
app.delete("/service/labels/:id", function(req, res) {
  labelService.delete(req, res);
});

/**
 * HTTP GET request to retrieve attachments
 *
 * @param route - Routes HTTP GET requests to the specified path
 * @param callback function - Calls a service method to get attachments
 */
app.get("/service/attachments", function(req, res) {
  attachmentService.getAttachment(req, res);
});

/**
 * HTTP POST request to index user uploaded resumes
 *
 * @param route - Routes HTTP POST requests to the specified path
 * @param callback function - Calls a service method to index the attachments
 */
app.post("/service/uploads", function(req,res) {
  uploadService.indexUploads(req,res);
});

/**
 * HTTP GET request to retrieve terms for autocomplete based on user input
 *
 * @param route - Routes HTTP GET requests to the specified path
 * @param callback function - Calls a service method to query user input to find relavent terms
 */
app.get("/service/suggest", function(req, res) {
  var term = req.query.term.toLowerCase();
  applicantService.suggest(term, res);
});

/**
 * HTTP GET request for aggregations for analysis
 *
 * @param route - Routes HTTP GET requests to the specified path
 * @param callback function - Calls a service method to query user input to find relavent terms
 */
app.get("/service/analysis", function(req, res) {
  applicantService.aggregations(res, req.query.type, req.query.field, req.query.query);
});

root.use('/app', app);

/**
 * HTTP GET request on the app root 
 *
 * @param route - Routes HTTP GET requests to the specified path
 * @param callback function - redirects to homepage
 */
root.get("/", function(req, res) {
  res.redirect("/app");
});

/**
 * HTTP GET for logging out a user, redirects to home page
 *
 */
app.get('/service/logout', function(req, res){
  req.logout();
  res.redirect('/app');
});

// start the GitHub Login process
app.get('/service/auth', passport.authenticate('github'));

/**
 * HTTP GET for trying to authenticate user. if authentication fails, redirect to failure page
 *
 */
app.get('/service/auth/callback', passport.authenticate('github', { failureRedirect: '/app/admin/failure' }),
  function(req, res) {
    res.redirect('/app/admin');
  }
);

/**
 * HTTP GET for ensuring user is authenticated then redirecting to admin page
 *
 */
app.get('/admin', ensureAuthenticated, function(req, res) {
  // show user info
  // if (req.isAuthenticated()) {
  //   html += "<p>authenticated as user:</p>"
  //   html += "<pre>" + JSON.stringify(req.user, null, 4) + "</pre>";
  // }
  res.redirect("/app/admin/success");
});

/**
 * HTML5 mode, gets rid of the '#' in URLs
 *
 * @param route -
 * @param callback function - 
 */
app.all('/*', function(req, res) {
  res.sendFile('index.html', {
    root: __dirname + "/../client/"
  });
});


passport.serializeUser(function(user, done) {
  // placeholder for custom user serialization
  // null is for errors
  done(null, user);
});

passport.deserializeUser(function(user, done) {
  // placeholder for custom user deserialization.
  // maybe you are going to get the user from mongo by id?
  // null is for errors
  done(null, user);
});

var GithubStrategy = require('passport-github2').Strategy;

passport.use(new GithubStrategy({
    clientID:  "no",
    clientSecret: "no",
    callbackURL: "https://localhost:8082/app/service/auth/callback"
  },
  function(accessToken, refreshToken, profile, done) {
    return done(null, profile);
  }
));

// Simple middleware to ensure user is authenticated.
// Use this middleware on any resource that needs to be protected.
// If the request is authenticated (typically via a persistent login session),
// the request will proceed.  Otherwise, the user will be redirected to the
// login page.
function ensureAuthenticated(req, res, next) {
  if (req.isAuthenticated()) {
    // req.user is available for use here
    return next(); 
  }

  res.redirect('/app/admin/failure');
}

/**
 * function that reads in SSL key and certificate and serializes it
 *
 * @param ssl - ssl group that is read in from a file
 */
var readSsl = function(ssl) {
  if (ssl.key.charAt(0) != '/') {
    ssl.key = __dirname + '/' + ssl.key;
  }

  if (ssl.cert.charAt(0) != '/') {
    ssl.cert = __dirname + '/' + ssl.cert;
  }

  ssl = {
    key: fs.readFileSync(ssl.key),
    cert: fs.readFileSync(ssl.cert)
  }

  return ssl;
}

finalSsl = readSsl(options.ssl);

var server = https.createServer(finalSsl, root).listen(8082, function() {
  var host = server.address().address
  var port = server.address().port

  console.log("SmartHire listening at https://%s:%s", host, port);
});
