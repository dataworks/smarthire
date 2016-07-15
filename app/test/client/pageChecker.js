var webPage = require('webpage');
var page = webPage.create();

page.open('http://interns.dataworks-inc.com/app/applicants', function(status) {

  var title = page.evaluate(function() {
    return document.title;
  });

  console.log(title);

  if (title == 'SmartHire: Applicant Search') {
    console.log("Web app is named correctly")
  } else {
    console.log("Web app is named incorrectly")
  }
  phantom.exit();

});