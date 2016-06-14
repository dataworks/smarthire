applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', '$location', '$anchorScroll', 'Archive', '$window',
  function ($scope, Applicant, Favorite, $location, Archive, $anchorScroll, $window) {
    $scope.applicants = Applicant.query();

    $scope.selection = "Applicant";

    $scope.barLimit = 0;


    $scope.increaseLimit = function(){
        $scope.barLimit += 15;
        if($scope.barLimit >= $scope.applicants.length && $scope.barLimit > 15){
            angular.element("#footer").show();
        }
        console.log('Increase Bar Limit', $scope.barLimit, $scope.applicants.length)
    };

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


