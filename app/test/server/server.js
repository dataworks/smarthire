var expect = require("chai").expect;
var request = require("request");

describe("Applicant Server", function() {
  describe("New List", function() {
    var url = "http://localhost:8082/app/service/applicants";

    it("returns dummy applicants", function(done) {
      request(url, function(error, response, body) {
        expect(response.statusCode).to.equal(200);
        var data = JSON.parse(body);
        expect(data).to.have.length.above(0);

        done();
      });
    });
  });

  describe("Autocomplete", function() {
    var url = "http://localhost:8082/app/service/suggest?field=additionalInfo.resume&term=java";

    it("aggregation length is not equal to 5", function(done) {
      request(url, function(error, response, body) {
        var data = JSON.parse(body);
        expect(data).to.have.lengthOf(5);
        done();
      });
    });
  });

  describe("Attachments", function() {
    var url = "http://localhost:8082/app/service/attachments?id=00186ac4af79e884ad164394043005a6&type=pdf";

    it("checks the resume body to be a string", function(done) {
      request(url, function(error, response, body) {
        body.should.be.a.('string');
        done();
      });
    });
  });



});