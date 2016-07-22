applicantControllers.controller('AdminCtrl', ['$scope', 'Admin',
  function($scope, Admin) {

  	$scope.settings = Admin.query();

  }
]);
