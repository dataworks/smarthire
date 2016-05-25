applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant',
  function ($scope, Applicant) {
    $scope.applicants = Applicant.query();
  }]);
