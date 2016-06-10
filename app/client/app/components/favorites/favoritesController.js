applicantControllers.controller('FavoritesCtrl', ['$scope', 'Favorite',
  function ($scope, Favorite) {
  	$scope.favorites = Favorite.query();
 
  }]);