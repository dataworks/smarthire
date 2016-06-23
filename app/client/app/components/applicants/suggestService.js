//calls Suggest from the backend
applicantServices.factory('Suggest', ['$resource', function($resource) {
  return $resource('service/suggest');
}]);