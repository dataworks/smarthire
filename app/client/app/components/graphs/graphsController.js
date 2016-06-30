applicantControllers.controller('GraphsCtrl', ['$scope', 'Graphs', 
  function($scope, graphs) {

  	// $scope.queries = [$scope.languages, $scope.etl, $scope.web, 
  	// 	$scope.mobile, $scope.db, $scope.bigData];

  	// var fields = ['languages', 'etl', 'web', 'mobile', 'db', 'bigData'];
  	// var ids = ['Languages', 'ETL', 'Web', 'Mobile', 'Databases', 'Big'];

  	// for(var i = 0; i < 6; i++) {
  	// 	$scope.queries[i] = graphs.query ({
  	// 		field: fields[i]
  	// 	});

  	// 	$scope.queries[i].$promise.then(function(data) {
  	// 		displayGraph(data, ids[i]);
  	// 	});
  	// }

	  $scope.languages = graphs.query({
	    field: 'languages'
	  });

	  $scope.etl = graphs.query({
	  	field: 'etl'
	  });

	  $scope.web = graphs.query({
	  	field: 'web'
	  });

	  $scope.mobile = graphs.query({
	  	field: 'mobile'
	  });

	  $scope.db = graphs.query({
	  	field: 'db'
	  });

	  $scope.bigData = graphs.query({
	  	field: 'bigData'
	  });

	  $scope.languages.$promise.then(function(data) {
	  	displayGraph(data, "Language");
	  });

	  $scope.etl.$promise.then(function(data) {
	  	displayGraph(data, "ETL");
	  });

	   $scope.web.$promise.then(function(data) {
	  	displayGraph(data, "Web");
	  });

	  $scope.mobile.$promise.then(function(data) {
	  	displayGraph(data, "Mobile");
	  });

	  $scope.db.$promise.then(function(data) {
	  	displayGraph(data, "Databases");
	  });

	  $scope.bigData.$promise.then(function(data) {
	  	displayGraph(data, "Big");
	  });

	  function displayGraph(data, id) {
	  	var labels = data.map(function(index) {
        return index.key;
      });

      var count = data.map(function(index) {
        return index.doc_count;
      });

      var ctx = document.getElementById(id);

      var chart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            label: '# of Votes',
            data: count,
            backgroundColor: [
              '#0D47A1',
              '#1565C0',
              '#1976D2',
              '#1E88E5',
              '#2196F3',
              '#42A5F5',
              '#64B5F6',
              '#90CAF9',
              '#BBDEFB',
              '#E3F2FD'
            ],
            borderColor: [
              '#0D47A1',
              '#1565C0',
              '#1976D2',
              '#1E88E5',
              '#2196F3',
              '#42A5F5',
              '#64B5F6',
              '#90CAF9',
              '#BBDEFB',
              '#E3F2FD'
            ],
            borderWidth: 1
          }]
        }
      });
	  }
  }
]);