applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', '$location', '$anchorScroll', 'Archive', '$window',
  function ($scope, Applicant, Favorite, $location, Archive, $anchorScroll, $window) {

    

$scope.selection = "Applicant";
   $scope.index = 0;
   $scope.pageSize = 25;
   $scope.loadingData = false;
   $scope.hasData = true;
   
   $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});

   $scope.dataLoaded = function(result) {
       if (result.length > 0) {
           $scope.applicants = $scope.applicants.concat(result);
       }
       else {
           $scope.hasData = false;
           angular.element("#footer").show();
       }

       $scope.loadingData = false;
   };

   $scope.nextPage = function(){
       if ($scope.hasData) {
           $scope.loadingData = true;
           $scope.index += $scope.pageSize;

           Applicant.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);
       }
   };
    
//     $scope.selection = "Applicant";

//     $scope.index = 0;
//     $scope.pageSize = 50;
//     $scope.loadingData = false;
//     $scope.hasData = true;

//     $scope.applicants = Applicant.query({from: $scope.index, size: $scope.pageSize});


// // start over.  go through the array of applicants and work with loading 10 at a time
// // use basic demo

//     $scope.dataLoaded = function(result){
//         if(result.length > 0){
//         $scope.applicants = $scope.applicants.concat(result);
        
//         }
//         else{
//             $scope.hasData = false;
//             angular.element("#footer").show();
//         }

            
        

//         $scope.loadingData = false;
//     };

//     $scope.nextPage = function(){
//         if($scope.hasData){
//         $scope.index += $scope.pageSize;
//         $scope.loadingData = true;
//         Applicant.query({from: $scope.index, size: $scope.pageSize}, $scope.dataLoaded);
//         }
//     };

    //change for the others

    $scope.showSelectValue = function(type) {
        console.log(type);
        if (type == 'Favorite') {
            $scope.applicants = Favorite.query();
        }
        if (type == 'Archive') {
            $scope.applicants = Archive.query();
        }
        if (type == 'Applicant') {
            $scope.applicants = Applicant.query();
        }
    };

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

    // $scope.ret = function (id, type) {
    // 	var retObject = new RetObject({'id': id, 'type' : type});
    // 	retObject.$save();
    // }
  }]);


