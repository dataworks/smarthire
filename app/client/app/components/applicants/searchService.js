/**
  * function that takes in booleans based on the checkboxes and returns a query string for search
  *
  * @params check1-12- booleans that are determined by the checkbox in the advanced search menu
  * @return final - query string that is sent back to applicantController.js for search
  *
  */
applicantServices.factory('advancedSearch', function() {
  return {
    createQuery: function(document) {
      var initial = "";
      var final = "";
      //sets a boolean based on which checkboxes are checked
      var check1 = document.getElementById("csCheck").checked;
      var check2 = document.getElementById("cpeCheck").checked;
      var check3 = document.getElementById("itCheck").checked;
      var check4 = document.getElementById("mathCheck").checked;
      var check5 = document.getElementById("vaCheck").checked;
      var check6 = document.getElementById("mdCheck").checked;
      var check7 = document.getElementById("dcCheck").checked;
      var check8 = document.getElementById("paCheck").checked;
      var check9 = document.getElementById("uvaCheck").checked;
      var check10 = document.getElementById("jmuCheck").checked;
      var check11 = document.getElementById("rpiCheck").checked;
      var check12 = document.getElementById("gmuCheck").checked;
      var check13 = document.getElementById("devCheck").checked;
      var check14 = document.getElementById("arcCheck").checked;
      var check15 = document.getElementById("manCheck").checked;
      var check16 = document.getElementById("engCheck").checked;

      if (check1) {
        initial = initial + "'computer science'";
      }

      if (check2) {
        initial = initial + "'computer engineering'";
      }

      if (check3) {
        initial = initial + "'information technology'";
      }

      if (check4) {
        initial = initial + "'mathematic'";
      }

      if (initial != "") {
        final = final + " AND education.degree: (" + initial + ")";
      }

      initial = "";

      if (check5) {
        initial = initial + "'VA'";
      }

      if (check6) {
        initial = initial + "'MD'";
      }

      if (check7) {
        initial = initial + "'DC'";
      }

      if (check8) {
        initial = initial + "'PA'";
      }

      if (initial != "") {
        final = final + " AND currentLocation.location: (" + initial + ")";
      }

      initial = "";

      if (check9) {
        initial = initial + "'Virginia'";
      }

      if (check10) {
        initial = initial + "'James Madison'";
      }

      if (check11) {
        initial = initial + "'Rensselaer'";
      }

      if (check12) {
        initial = initial + "'George Mason'";
      }

      if (initial != "") {
        final = final + " AND education.school: (" + initial + ")";
      }

      initial = "";

      if (check13) {
        initial = initial + "'developer'";
      }

      if (check14) {
        initial = initial + "'architect'";
      }

      if (check15) {
        initial = initial + "'manager'";
      }

      if (check16) {
        initial = initial + "'engineer'";
      }

      if (initial != "") {
        final = final + " AND currentLocation.title: (" + initial + ")";
      }

      return final;
    }

  }
});