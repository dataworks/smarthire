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