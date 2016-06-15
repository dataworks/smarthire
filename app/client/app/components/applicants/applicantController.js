applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', '$location', '$anchorScroll', 'Archive', '$window',
  function ($scope, Applicant, Favorite, $location, $anchorScroll, Archive, $window) {

   $scope.selection = "Applicant";
   $scope.index = 0;
   $scope.pageSize = 25;
   $scope.loadingData = false;
   $scope.hasData = true;

   $scope.queryType="";
   
   $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});

    $scope.showSelectValue = function(type) {
        //console.log(type);
        if (type == 'Favorite') {
            // $scope.index = 0;
            $scope.hasData = true;
            $scope.selection = "Favorite";
            $scope.applicants = Favorite.query({from: $scope.index, size: $scope.pageSize});;
        }
        if (type == 'Archive') {
            // $scope.index = 0;
            $scope.hasData = true;
            $scope.selection = "Archive";
            $scope.applicants = Archive.query({from: $scope.index, size: $scope.pageSize});
        }
        if (type == 'Applicant') {
           // $scope.index = 0;
           $scope.hasData = true;
            $scope.selection = "Applicant";
            $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});
        }
    };
    

    $scope.dataLoaded = function(result) {
       if (result.length > 0) {
           $scope.applicants = $scope.applicants.concat(result);
           angular.element("#footer").hide();
       }
       else {
           $scope.hasData = false;
           $scope.index = 0;
           angular.element("#footer").show();
       }

       $scope.loadingData = false;
   };

   $scope.nextPage = function(){
       if ($scope.hasData) {
           $scope.loadingData = true;
           $scope.index += $scope.pageSize;

           if ($scope.selection == "Applicant") {
                Applicant.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);

           }
            
            if ($scope.selection == "Favorite") {
                Favorite.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);

           }
            
            else {
                Archive.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);

           }

        
       }
   };
    
    //change for the others

// Only enable if the document has a long scroll bar
// Note the window height + offset
if ( $(window).scrollY > 13) {
    $('#top-link-block').removeClass('hidden').affix({
        // how far to scroll down before link "slides" into view
        offset: {top:100}
    });
}


    $scope.mark = function (id, type) {
    	var favorite = new Favorite({'id': id, 'type' : type});
        var y = $window.scrollY;
        console.log(y);
        favorite.$save().then(function(){
            $scope.applicants = Applicant.query();
            // $location.hash(' ');
            // $anchorScroll();

            // $window.scrollTo(500,500);
            // // $anchorScroll.yOffset = y;
            // // $anchorScroll();
        });
    };

    $scope.button = function(text){
        alert(text);
    }

    // $scope.ret = function (id, type) {
    // 	var retObject = new RetObject({'id': id, 'type' : type});
    // 	retObject.$save();
    // }
  }]);


