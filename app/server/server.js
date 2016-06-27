var express = require("express");
var bodyParser = require("body-parser");
var elasticsearch = require("elasticsearch");
var passport = require('passport');
var util = require('util');
var session = require('express-session');
var methodOverride = require('method-override');
var GitHubStrategy = require('passport-github2').Strategy;
var partials = require('express-partials');
var root = express();
var app = express();

var applicantService = require("./services/applicants.js");
var labelService = require("./services/labels.js");
var attachmentService = require("./services/attachments.js");
var uploadService = require("./services/uploads.js");

//used for static files (html, client js, images)
app.use(express.static("client"));

app.use(express.static("node_modules"));

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
  uploadService.index(req,res);
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
 * HTTP GET request on the app root 
 *
 * @param route - Routes HTTP GET requests to the specified path
 * @param callback function - redirects to homepage
 */
root.get("/", function(req, res) {
  res.redirect("/app");
});

root.use('/app', app);

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

var server = root.listen(8082, function() {
  var host = server.address().address
  var port = server.address().port

  console.log("ResCheck listening at http://%s:%s", host, port)
});

//authentication code

var GITHUB_CLIENT_ID = "";
var GITHUB_CLIENT_SECRET = "";

// Passport session setup.
//   To support persistent login sessions, Passport needs to be able to
//   serialize users into and deserialize users out of the session.  Typically,
//   this will be as simple as storing the user ID when serializing, and finding
//   the user by ID when deserializing.  However, since this example does not
//   have a database of user records, the complete GitHub profile is serialized
//   and deserialized.
passport.serializeUser(function(user, done) {
  done(null, user);
});

passport.deserializeUser(function(obj, done) {
  done(null, obj);
});

// Use the GitHubStrategy within Passport.
//   Strategies in Passport require a `verify` function, which accept
//   credentials (in this case, an accessToken, refreshToken, and GitHub
//   profile), and invoke a callback with a user object.
passport.use(new GitHubStrategy({
    clientID: GITHUB_CLIENT_ID,
    clientSecret: GITHUB_CLIENT_SECRET,
    callbackURL: "http://localhost:8082/app/auth/github/callback"
  },
  function(accessToken, refreshToken, profile, done) {
    // asynchronous verification, for effect...
    process.nextTick(function () {
      
      // To keep the example simple, the user's GitHub profile is returned to
      // represent the logged-in user.  In a typical application, you would want
      // to associate the GitHub account with a user record in your database,
      // and return that user instead.
      return done(null, profile);
    });
  }
));