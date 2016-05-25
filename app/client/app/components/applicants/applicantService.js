applicantServices.factory('Applicant', ['$resource', function($resource) {
  return $resource('applicants');
}]);
