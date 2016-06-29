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

      var ctx = document.getElementById("myChart");

      var myChart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            label: '# of Votes',
            data: data,
            backgroundColor: [
              '#F44336',
              '#2196F3',
              '#FFEB3B',
              '#4CAF50',
              '#9C27B0',
              '#FFA726',
              '#E91E63',
              '#3F51B5'
            ],
            borderColor: [
              '#EF9A9A',
              '#90CAF9',
              '#FFF59D',
              '#A5D6A7',
              '#CE93D8',
              '#FFCC80',
              '#F48FB1',
              '#9FA8DA'
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