var expect  = require("chai").expect;
var request = require("request");

describe("Applicant Server", function() {
    describe("Applicants List", function() {
      var url = "http://localhost:8082/applicants";

      it("returns dummy applicants", function(done) {
        request(url, function(error, response, body) {
          expect(response.statusCode).to.equal(200);

          var data = JSON.parse(body);
          expect(data).to.have.length(4);
          expect(data[0].name).to.equal('Joe Schweitzer');

          done();
        });
      });
    });

    describe("Search", function() {
      var url = "http://localhost:8082/search";

      it("returns dummy applicants", function(done) {
        request(url, function(error, response, body) {
          expect(response.statusCode).to.equal(200);

          var data = JSON.parse(body);
          expect(data).to.have.length(4);
          expect(data[0].name).to.equal('Joe Schweitzer');

          done();
        });
      });
    });
});
