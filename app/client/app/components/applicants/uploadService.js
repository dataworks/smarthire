//calls Upload from the backend
applicantServices.factory('Upload', ['$resource', function($resource) {
  return $resource('service/uploads');
}]);