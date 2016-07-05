//calls graph from the backend
applicantServices.factory('Analysis', ['$resource', function($resource) {
  return $resource('service/analysis');
}]);