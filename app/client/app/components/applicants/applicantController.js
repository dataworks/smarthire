applicantControllers.controller('ApplicantCtrl', ['$scope', 'Applicant', 'Favorite', 
  function ($scope, Applicant, Favorite) {
    $scope.filter = function(type) {
        console.log(type);
        
    };

    $scope.applicants = Applicant.query();
    $scope.mark = function (id, type) {
    	var favorite = new Favorite({'id': id, 'type' : type});
		favorite.$save();
    };


    // $scope.ret = function (id, type) {
    // 	var retObject = new RetObject({'id': id, 'type' : type});
    // 	retObject.$save();
    // }
  }]);


