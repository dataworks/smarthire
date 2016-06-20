applicantServices.factory('Applicant', ['$resource', function($resource) {
	return $resource('service/applicants/:id');
}]);