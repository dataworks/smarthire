/**
  * function that takes in booleans based on the checkboxes and returns a query string for search
  *
  * @params check1-12- booleans that are determined by the checkbox in the advanced search menu
  * @return final - query string that is sent back to applicantController.js for search
  *
  */
applicantServices.factory('advancedSearch', function() {
  return {
    createQuery: function(check1, check2, check3, check4, check5, check6, check7, check8, check9, check10, check11, check12) {
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

      if (initial != "") {
        final = final + " AND education.degree: (" + initial + ")";
      }

      initial = "";

      if (check4) {
        initial = initial + "'VA'";
      }

      if (check5) {
        initial = initial + "'MD'";
      }

      if (check6) {
        initial = initial + "'DC'";
      }

      if (initial != "") {
        final = final + " AND currentLocation.location: (" + initial + ")";
      }

      initial = "";

      if (check7) {
        initial = initial + "'Virginia'";
      }

      if (check8) {
        initial = initial + "'James Madison'";
      }

      if (check9) {
        initial = initial + "'Rensselaer'";
      }

      if (initial != "") {
        final = final + " AND education.school: (" + initial + ")";
      }

      initial = "";

      if (check10) {
        initial = initial + "'developer'";
      }

      if (check11) {
        initial = initial + "'architect'";
      }

      if (check12) {
        initial = initial + "'manager'";
      }

      if (initial != "") {
        final = final + " AND currentLocation.title: (" + initial + ")";
      }

      return final;
    }

  }
});