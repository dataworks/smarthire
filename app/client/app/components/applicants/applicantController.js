applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant',
  function ($scope, Applicant) {
    $scope.applicants = Applicant.query();
    $scope.markFavorite = function (id) {
    	console.log(id);
    };
  }]);


