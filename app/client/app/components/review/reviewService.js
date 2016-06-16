applicantServices.factory('Review', ['$resource', function($resource) {
  return $resource('service/review');
}]);