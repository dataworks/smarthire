applicantControllers.controller('GraphsCtrl', ['$scope', 'Graphs', 
  function($scope, graphs) {

  	$scope.queries = [$scope.languages, $scope.etl, $scope.web, 
  		$scope.mobile, $scope.db, $scope.bigData];

  	var fields = ['languages', 'etl', 'web', 'mobile', 'db', 'bigData'];
  	var ids = ['Language', 'ETL', 'Web', 'Mobile', 'Databases', 'Big'];

  	$scope.queries.forEach(function(value, index) {
  		$scope.queries[index] = graphs.query ({
  			field: fields[index]
  		});

  		$scope.queries[index].$promise.then(function(data) {
  			console.log(index);
  			console.log(data);
  			displayGraph(data, ids[index]);
  		});
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
              '#E3F2FD',
              '#BBDEFB',
              '#90CAF9',
              '#64B5F6',
              '#42A5F5',
              '#2196F3',
              '#1E88E5',
              '#1976D2',
              '#1565C0',
              '#0D47A1'
            ],
            borderColor: [
              '#E3F2FD',
              '#BBDEFB',
              '#90CAF9',
              '#64B5F6',
              '#42A5F5',
              '#2196F3',
              '#1E88E5',
              '#1976D2',
              '#1565C0',
              '#0D47A1'
            ],
            borderWidth: 1
          }]
        }
      });
	  }
  }
]);