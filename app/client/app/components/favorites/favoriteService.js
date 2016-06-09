applicantServices.factory('Favorite', ['$resource', function($resource) {
  return $resource('service/favorites/:id');
}]);

