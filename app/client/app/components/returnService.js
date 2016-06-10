applicantServices.factory('RetObject', ['$resource', function($resource) {
  return $resource('service/returns/:id');
}]);