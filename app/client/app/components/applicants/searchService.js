/**
  * function that takes in booleans based on the checkboxes and returns a query string for search
  *
  * @params check1-12- booleans that are determined by the checkbox in the advanced search menu
  * @return final - query string that is sent back to applicantController.js for search
  *
  */
applicantServices.factory('advancedSearch', function() {
  return {
    createQuery: function(check1, check2, check3, check4, check5, check6, check7, check8, check9, check10, check11, check12, check13, check14, check15, check16) {
      var initial = "";
      var final = "";

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