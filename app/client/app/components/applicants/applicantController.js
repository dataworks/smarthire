applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', '$location', '$anchorScroll', 'Archive', 
  function ($scope, Applicant, Favorite, $location, Archive, $anchorScroll) {
    $scope.applicants = Applicant.query();

    $scope.selection = "Applicant";

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
		// favorite.$save();
        favorite.$save().then(function(){
            $scope.applicants = Applicant.query();
        });
    };

    // $scope.ret = function (id, type) {
    // 	var retObject = new RetObject({'id': id, 'type' : type});
    // 	retObject.$save();
    // }
  }]);


