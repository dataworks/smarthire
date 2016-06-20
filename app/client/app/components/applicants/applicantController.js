applicantControllers.controller('ApplicantCtrl', ['$scope', '$location', 'Applicant', 'Label', '$window',
  function ($scope, $location, Applicant, Label, $window) {

    //default dropdown menu to 'new' on page load
    $scope.selection = "new";

    //query should start off at index 0, displaying first item
    $scope.index = 0;
    //displaying 25 applicants at a time
    $scope.pageSize = 25;
    $scope.loadingData = false;
    $scope.hasData = true;

    //sorting table by column code
    $scope.propertyName = null;
    $scope.reverse = false;
    //when column is clicked, call this function to sort
    $scope.sortBy = function(propertyName) {
      $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
      $scope.propertyName = propertyName;
    };
     
     $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});

    //change queries when new type is selected from the dropdown menu
    $scope.showSelectValue = function(type) {
      $scope.index = 0;
      $scope.hasData = true;
      $scope.selection = type;
      $scope.applicants = Applicant.query({type: type, from: $scope.index, size: $scope.pageSize});
    };

    //if there is more data, add to current query
    //else there is no data
    $scope.dataLoaded = function(result) {
      if (result.length > 0) {
        $scope.applicants = $scope.applicants.concat(result);
      }
      else {
        $scope.hasData = false;
        $scope.index = 0;
      }

      $scope.loadingData = false;
    };

    //check if there is more data to load from the query, for infinite scroll
    $scope.nextPage = function() {
      if ($scope.hasData) {
        $scope.loadingData = true;
        $scope.index += $scope.pageSize;
        Applicant.query({type: $scope.selection, from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);
      };
    }

    // Only enable if the document has a long scroll bar
    // Note the window height + offset
    $('#top-link-block').removeClass('hidden').affix({
      offset: {top:100}
    });

    //function that is called when action button is clicked
    //i.e. Favorite, Archive, Review
    $scope.mark = function (id, type, applicant) {
    	var label = new Label({'id': id, 'type' : type});
        label.$save().then(function() {
          $scope.applicants.splice($scope.applicants.indexOf(applicant), 1);
      });
    }

    //function that is called when applicant is placed back in 'New'
    $scope.remove = function(id, applicant) {
      Label.delete({'id': id}).$promise.then(function() {
        $scope.applicants.splice($scope.applicants.indexOf(applicant), 1);
      });
   }

    //code for toast messages
    $scope.showToast = function(id) {
      //change toast CSS to show the message
      document.getElementById(id).style.display = "block";
      console.log("i am here");
      //after 3 seconds, hide the toast
      setTimeout($scope.hideToast, 3000, id);
    }

    $scope.hideToast = function(id) {
      //change toast CSS to hide the message
      document.getElementById(id).style.display = "none";
      console.log("now i am here");
    }


    //code for searching
    $scope.foundPeople = [];
    $scope.searchTracker = 0;
    $scope.allResults = false;
    $scope.searchTerm = $location.search().q

    $scope.search = function() {
      $scope.foundPeople = [];
      $scope.searchTracker = 0;
      $scope.allResults = false;
      $location.search({'q': $scope.searchTerm});
      $scope.loadMore();
    }

    $scope.loadMore = function() {
      foundPeople.search($scope.searchTerm, $scope.searchTracker++).then(function(results) {
        if (results.length !== 10) {
          $scope.allResults = true;
        }
        var i = 0;

        for (; i < results.length; i++) {
          $scope.foundPeople.push(results[i]);
        }
      });
    };

    //scroll code
    $(function(){
      var lastScrollTop = 0, delta = 5;
      $(window).scroll(function(event){
        var st = $(this).scrollTop();
     
      if(Math.abs(lastScrollTop - st) <= delta)
        return;
  
      //if scrolling up, hide the footer
      if (st <= lastScrollTop){;
        angular.element("#footer").hide();
       } 
        
        lastScrollTop = st;
      });
    });

    //if at bottom of window, show footer 
    $(window).scroll(function() {   
      if($(window).scrollTop() + $(window).height() == $(document).height()) {
        angular.element("#footer").show();
      }
      else{
        angular.element("#footer").hide();
      }
    });
}]);
