applicantControllers.controller('GraphsCtrl', ['$scope', 'Graphs', 
  function($scope, graphs) {
  	
	  $scope.count = graphs.query({
	    field: 'skills.language'
	  });

    $scope.count.$promise.then(function(data) {

      var labels = data.map(function(index) {
        return index.term;
      });

      var data = data.map(function(index) {
        return index.count;
      });

      var ctx = document.getElementById("Language");

      var Language = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            label: '# of Votes',
            data: data,
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
        },
        // options: {
        //     scales: {
        //         yAxes: [{
        //             ticks: {
        //                 beginAtZero:true
        //             }
        //         }]
        //     }
        // }
      });
    });
  }
]);