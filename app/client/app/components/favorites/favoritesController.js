applicantControllers.controller('FavoritesCtrl', ['$scope', 'Favorite',
  function ($scope, Favorite) {
  	$scope.labels = Favorite.query();
 
  }]);