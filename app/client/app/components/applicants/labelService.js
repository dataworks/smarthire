applicantServices.factory('Label', ['$resource', function($resource) {
  return $resource('service/labels');
}]);
