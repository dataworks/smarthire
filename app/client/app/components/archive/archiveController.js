applicantControllers.controller('ArchiveCtrl', ['$scope', 'Archive',
  function ($scope, Archive) {
 	$scope.labels = Archive.query();
  }]);