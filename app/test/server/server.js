var expect  = require("chai").expect;
var request = require("request");

describe("Favorite Server", function() {
    describe("Favorites List", function() {
      var url = "http://localhost:8082/app/service/favorites";

      it("returns dummy applicants", function(done) {
        request(url, function(error, response, body) {
          expect(response.statusCode).to.equal(200);

          var data = JSON.parse(body);
          expect(data).to.have.length(3);
          //expect(data[0].name).to.equal('Dave Mezzetti');

          done();
        });
      });
    });

    describe("Archive Server", function() {
      var url = "http://localhost:8082/app/service/archive";

      it("returns dummy applicants", function(done) {
        request(url, function(error, response, body) {
          expect(response.statusCode).to.equal(200);

          var data = JSON.parse(body);
          expect(data).to.have.length(3);
          //expect(data[0].name).to.equal('Dave Mezzetti');

          done();
        });
      });
    });
});
