applicantControllers.controller('HeaderCtrl', ['$scope', '$location',
  function($scope, $location) {
    $scope.isActive = function(viewLocation) {
      return $location.path().indexOf(viewLocation) == 0;
    };
  }
]);