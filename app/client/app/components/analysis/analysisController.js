applicantControllers.controller('AnalysisCtrl', ['$scope', 'Analysis',
  function($scope, analysis) {

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
        displayGraph(data, ids[index]);
      });
    });

    /**
     * Stores the aggregation data into arrays & calls function to display charts
     * 
     * @param data - aggregation data
     * @param id - div id of the chart
     */
    function displayGraph(data, id) {
      var labels = data.map(function(index) {
        return index.key;
    });

      var count = data.map(function(index) {
        return index.doc_count;
      });

      var ctx = document.getElementById(id);

      var blues = [
        '#0D47A1',
        '#1976D2',
        '#2196F3',
        '#64B5F6',
        '#BBDEFB'
      ];

      var reds = [
        // dark to light mix of two color groups
        '#B71C1C',
        '#FF3232',
        '#FF6666',
        '#FF9999',
        '#FFCCCC'
      ];

      var greens = [
        '#1B5E20',
        '#388E3C',
        '#4CAF50',
        '#81C784',
        '#C8E6C9'
      ];

      var oranges = [
        '#E65100',
        '#F57C00',
        '#FF9800',
        '#FFB74D',
        '#FFE0B2'
      ];

      var yellows = [
        '#F57F17',
        '#FBC02D',
        '#FFEB3B',
        '#FFF176',
        '#ffff99'
      ];

      var purples = [
        '#4A148C',
        '#7B1FA2',
        '#9C27B0',
        '#BA68C8',
        '#E1BEE7'
      ];

      if (id == 'Language') {
        createPieChart(ctx, labels, count, reds);
      }

      if (id == 'Web') {
        createPieChart(ctx, labels, count, oranges);
      }

      if (id == 'ETL') {
        createPieChart(ctx, labels, count, yellows);
      }

      if (id == 'Mobile') {
        createPieChart(ctx, labels, count, greens);
      }

      if (id == 'Databases') {
        createPieChart(ctx, labels, count, blues);
      }

      if (id == 'Big') {
        createPieChart(ctx, labels, count, purples);

      }
    }

    /**
     * Creates a pie chart using the chart.js library
     *
     * @param ctx - id of the chart
     * @param labels - terms
     * @param count - number of occurances
     * @param color - color scheme of the chart
     */
    function createPieChart(ctx, labels, count, color) {
      var chart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            label: '# of Votes',
            data: count,
            backgroundColor: color,
            borderColor: color,
            borderWidth: 1
          }]
        }
      });
    }
  
  }
]);