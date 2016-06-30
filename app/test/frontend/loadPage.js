var page = require('webpage').create(),
  system = require('system'),
  t;

t = Date.now();
address = system.args[1];
page.open('http://interns.dataworks-inc.com/app/applicants', function(status) {
  console.log("Status: " + status)
  if (status !== 'success') {
    console.log('FAIL to load the address');
  } else {
  	page.render('screenshot.png');
    t = Date.now() - t;
    console.log('Loading SmartHire');
    console.log('Loading time ' + t + ' msec');
  }
  phantom.exit();
});