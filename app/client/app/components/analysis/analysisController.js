applicantControllers.controller('AnalysisCtrl', ['$scope', 'Analysis', 'chartService',
  function($scope, analysis, chartService) {

    $scope.queries = [$scope.languages, $scope.etl, $scope.web,
      $scope.mobile, $scope.db, $scope.bigData
    ];

    var fields = ['languages', 'etl', 'web', 'mobile', 'db', 'bigData'];
    var ids = ['Language', 'ETL', 'Web', 'Mobile', 'Databases', 'Big'];

    /**
     * Run through each query and get the top 5 terms
     *
     * @param value - comes with function from array.prototype.foreach
     * @param index - current value in the arrays
     */
    $scope.queries.forEach(function(value, index) {
      $scope.queries[index] = analysis.query({
        field: fields[index]
      });

      $scope.queries[index].$promise.then(function(data) {
        $scope.showGraph(data, ids[index]);
      });
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