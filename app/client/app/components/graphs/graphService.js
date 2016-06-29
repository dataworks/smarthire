//calls graph from the backend
applicantServices.factory('Graphs', ['$resource', function($resource) {
  return $resource('service/graphs');
}]);