applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Label', '$window',
  function ($scope, Applicant, Label, $window) {

    $scope.selection = "new";
    $scope.index = 0;
    $scope.pageSize = 25;
    $scope.loadingData = false;
    $scope.hasData = true;

     //sorting table by column code
    $scope.propertyName = null;
    $scope.reverse = false;
    $scope.sortBy = function(propertyName) {

    $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
      $scope.propertyName = propertyName;
    };
     
    $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});

    $scope.showSelectValue = function(type) {
      $scope.index = 0;
      $scope.hasData = true;
      $scope.selection = type;
      $scope.applicants = Applicant.query({type: type, from: $scope.index, size: $scope.pageSize});
    };

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

    $scope.nextPage = function() {
      console.log($scope.selection, $scope.index);
      if ($scope.hasData) {
        $scope.loadingData = true;
        $scope.index += $scope.pageSize;

        Applicant.query({type: $scope.selection, from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);
      }
    };


    // Only enable if the document has a long scroll bar
    // Note the window height + offset
    //if ( $(window).height() > $(document).height()) {
    $('#top-link-block').removeClass('hidden').affix({
      // how far to scroll down before link "slides" into view
      offset: {top:100}
    });

    $scope.mark = function (id, type, index) {
    	var label = new Label({'id': id, 'type' : type});
        label.$save().then(function() {
          $scope.applicants.splice(index, 1);
      });
    }

    $scope.remove = function(id, index) {
      var label = new Label({'id' : id});
        label.$save().then(function() {
          $scope.applicants.splice(index, 1);
        });
    }


    //code for toast messages
    $scope.showToast = function(id) {
      document.getElementById(id).style.display = "block";
      setTimeout($scope.hideToast, 3000, id);
    }

    $scope.hideToast = function(id) {
      console.log(id);
      document.getElementById(id).style.display = "none";
    }


    //scroll code
    $(function(){
      var lastScrollTop = 0, delta = 5;
      $(window).scroll(function(event){
        var st = $(this).scrollTop();
     
        if(Math.abs(lastScrollTop - st) <= delta)
          return;
  
        if (st > lastScrollTop){
           // downscroll code
           //console.log('scroll down');
           //console.log($(window).scrollTop());
        } else {
          // upscroll code
         // console.log('scroll up');
         // console.log($(window).scrollTop());
          angular.element("#footer").hide();
        }
        lastScrollTop = st;
      });
    });

    $(window).scroll(function() {   
      if($(window).scrollTop() + $(window).height() == $(document).height()) {
        angular.element("#footer").show();
      }
    });

}]);