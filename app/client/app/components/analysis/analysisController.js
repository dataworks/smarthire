applicantControllers.controller('AnalysisCtrl', ['$scope', 'Analysis', 'chartService',
  function($scope, analysis, chartService) {

    $scope.queries = [$scope.languages, $scope.etl, $scope.web,
      $scope.mobile, $scope.db, $scope.bigData
    ];

    var ids = ['Big', 'Language', 'Web', 'Mobile', 'ETL', 'Databases'];

    analysis.query().$promise.then(function(data) {
      for(var i = 0; i < data.length; i++) {
        var arr = Object.keys(data[i]).map(function(key) {
          return data[i][key]
        });
        chartService.displayGraph(arr, ids[i]);
      }
    });

    /**
     * calls function in chartService to display pie charts
     * 
     * @param data - aggregation data
     * @param id - div id of the chart
     */
    $scope.showGraph = function(data, id) {
      chartService.displayGraph(data, id);
    }
  
  }
]);