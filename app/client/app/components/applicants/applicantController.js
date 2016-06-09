applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite',
  function ($scope, Applicant, Favorite) {
    $scope.applicants = Applicant.query();
    $scope.mark = function (id, type) {
    	var favorite = new Favorite({'id': id, 'type' : type});
		favorite.$save();
    };
  }]);


