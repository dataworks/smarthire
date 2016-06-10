applicantServices.factory('Archive', ['$resource', function($resource) {
  return $resource('service/archive');
}]);