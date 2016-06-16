applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', '$location', 'Archive', '$window', 'Review',
  function ($scope, Applicant, Favorite, $location, Archive, $window, Review) {

     $scope.selection = "Applicant";
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
        if (type == 'Favorite') {
            $scope.index = 0;
            $scope.hasData = true;
            $scope.selection = "Favorite";
            $scope.applicants = Favorite.query({from: $scope.index, size: $scope.pageSize});
        }
        if (type == 'Archive') {
            $scope.index = 0;
            $scope.hasData = true;
            $scope.selection = "Archive";
            $scope.applicants = Archive.query({from: $scope.index, size: $scope.pageSize});
        }
        if (type == 'Applicant') {
           $scope.index = 0;
           $scope.hasData = true;
           $scope.selection = "Applicant";
           $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});
       }
       if (type == 'Review'){
           $scope.index = 0;
           $scope.hasData = true;
           $scope.selection = "Review";
           $scope.applicants = Review.query({from: $scope.index, size: $scope.pageSize});
       }
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

 $scope.nextPage = function(){
    //console.log($scope.selection, $scope.index);
     if ($scope.hasData) {
         $scope.loadingData = true;
         $scope.index += $scope.pageSize;

         if ($scope.selection == "Applicant") {
            console.log("1");
            Applicant.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);

        }
    
        else if ($scope.selection == "Favorite") {
            console.log(2);
            Favorite.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);

        }  

        else if ($scope.selection == 'Review'){
            Review.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);
        }

        else {
            Archive.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);

        }

    }
};


// Only enable if the document has a long scroll bar
// Note the window height + offset
//if ( $(window).height() > $(document).height()) {
    $('#top-link-block').removeClass('hidden').affix({
        // how far to scroll down before link "slides" into view
        offset: {top:100}
    });
//}

    $scope.mark = function (id, type, index) {
    	var favorite = new Favorite({'id': id, 'type' : type});
        favorite.$save().then(function(){
            // $scope.applicants = Applicant.query();
            $scope.applicants.splice(index, 1);
        });
    };

    $scope.button = function(text){
        alert(text);
    }

    // $scope.ret = function (id, type) {
    // 	var retObject = new RetObject({'id': id, 'type' : type});
    // 	retObject.$save();
    // }

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