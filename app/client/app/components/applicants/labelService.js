//calls Label from the backend
applicantServices.factory('Label', ['$resource', function($resource) {
  return $resource('service/labels/:id');
}]);