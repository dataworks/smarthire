applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', '$location', 'Archive', 
  function ($scope, Applicant, Favorite, $location, Archive) {


    $scope.applicants = Applicant.query();
    $scope.filter = function(type) {
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


    //$scope.fav = Favorite.query();
    //$scope.archive = Archive.query();

    $scope.mark = function (id, type) {
    	var favorite = new Favorite({'id': id, 'type' : type});
		favorite.$save();
    };

    // $scope.ret = function (id, type) {
    // 	var retObject = new RetObject({'id': id, 'type' : type});
    // 	retObject.$save();
    // }
  }]);


