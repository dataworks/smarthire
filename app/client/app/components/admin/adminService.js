//calls Setting from the backend
applicantServices.factory('Admin', ['$resource', function($resource) {
 return $resource('service/settings', null, {
  'query': {
    method: 'GET',
    isArray: false
  }
 });
}]);