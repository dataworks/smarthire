var expect = require("chai").expect;
var request = require("request");
var supertest = require("supertest");
var should = require("should");

var server = supertest.agent("https://localhost:8082/app");


describe("Applicant Server", function() {
  describe("New List", function() {
    var url = "https://localhost:8082/app/service/applicants";

    it("returns applicants from Elasticsearch", function(done) {
      request(url, function(error, response, body) {
        expect(response.statusCode).to.equal(200);
        var data = JSON.parse(body);
        console.log(data.size);
        expect(data.size).to.be.above(0);

        done();
      });
    });
  });

  describe("Autocomplete", function() {
    var url = "https://localhost:8082/app/service/suggest?field=additionalInfo.resume&term=java";

    it("should be have a length of 5", function(done) {
      request(url, function(error, response, body) {
        var data = JSON.parse(body);
        expect(data).to.have.lengthOf(5);
        done();
      });
    });
  });

  describe("Labels POST", function() {
    it("should POST dummy applicant", function(done) {
      server
        .post('/service/labels')
        .send({"type" : "favorite", "id" : "123"})
        .expect("Content-type",/json/)
        .expect(200)
        .end(function(err,res){
          res.status.should.equal(200);
          done();
        });
    });
  });

  describe("Labels DELETE", function() {
    it("should DELETE dummy applicant", function(done) {
      server
        .delete('/service/labels/123')
        .expect("Content-type",/json/)
        .expect(200)
        .end(function(err,res){
          res.status.should.equal(200);
          done();
        });
    });
  });
});