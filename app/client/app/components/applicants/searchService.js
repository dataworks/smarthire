//function for creating an advanced search query
applicantServices.factory('advancedSearch', function() {
  return {
    createQuery: function(initial, check1, check2, check3, type) {
      var final = "";
      if (type == "major") {
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
      }

      else if (type == "location") {
        if (check1) {
          initial = initial + "'VA'";
        }

        if (check2) {
          initial = initial + "'MD'";
        }

        if (check3) {
          initial = initial + "'DC'";
        }

        if (initial != "") {
          final = final + " AND currentLocation.location: (" + initial + ")";
        }
      }

      else if (type == "education") {
        if (check1) {
          initial = initial + "'Virginia'";
        }

        if (check2) {
          initial = initial + "'James Madison'";
        }

        if (check3) {
          initial = initial + "'Rensselaer'";
        }

        if (initial != "") {
          final = final + " AND education.school: (" + initial + ")";
        }
      }

      else if (type == "position") {
        if (check1) {
          initial = initial + "'developer'";
        }

        if (check2) {
          initial = initial + "'architect'";
        }

        if (check3) {
          initial = initial + "'manager'";
        }

        if (initial != "") {
          final = final + " AND currentLocation.title: (" + initial + ")";
        }
      }

      return final;
    }

  }
});