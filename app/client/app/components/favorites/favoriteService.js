applicantServices.factory('Favorite', ['$resource', function($resource) {
  return $resource('service/favorites/:Id', {Id:'@._id'});
}]);

var favorite = new Favorite({id: '0123456789abcdef'});
favorite.$save();