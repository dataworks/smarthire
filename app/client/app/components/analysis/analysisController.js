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
        // '#E3F2FD',
        // '#BBDEFB',
        // '#90CAF9',
        // '#64B5F6',
        // '#42A5F5',
        // '#2196F3',
        // '#1E88E5',
        // '#1976D2',
        // '#1565C0',
        // '#0D47A1'
        '#0D47A1',
        '#1976D2',
        '#2196F3',
        '#64B5F6',
        '#BBDEFB'
      ];

      var reds = [
        // light to dark
        // '#FFEBEE',
        // '#FFCDD2',
        // '#EF9A9A',
        // '#E57373',
        // '#EF5350',
        // '#F44336',
        // '#E53935',
        // '#D32F2F',
        // '#C62828',
        // '#B71C1C'
        // dark to light google
        // '#B71C1C',
        // '#C62828',
        // '#D32F2F',
        // '#E53935',
        // '#F44336',
        // '#EF5350',
        // '#E57373',
        // '#EF9A9A',
        // '#FFCDD2',
        // '#FFEBEE'
        // dark to light mix of two color groups
        '#B71C1C',
        '#FF3232',
        '#FF6666',
        '#FF9999',
        '#FFCCCC'
      ];

      var greens = [
        // '#E8F5E9',
        // '#C8E6C9',
        // '#A5D6A7',
        // '#81C784',
        // '#66BB6A',
        // '#4CAF50',
        // '#43A047',
        // '#388E3C',
        // '#2E7D32',
        // '#1B5E20'
        '#1B5E20',
        '#388E3C',
        '#4CAF50',
        '#81C784',
        '#C8E6C9'
      ];

      var oranges = [
        // from light to dark google 
        // '#FBE9E7',
        // '#FFCCBC',
        // '#FFAB91',
        // '#FF8A65',
        // '#FF7043',
        // '#FF5722',
        // '#F4511E',
        // '#E64A19',
        // '#D84315',
        // '#BF360C'
        // from dark to light google
        // '#BF360C',
        // '#D84315',	
        // '#E64A19',
        // '#F4511E',
        // '#FF5722',
        // '#FF7043',
        // '#FF8A65',
        // '#FFAB91',
        // '#FFCCBC',
        // '#FBE9E7'
        // from dark to light other site
        '#E65100',
        '#F57C00',
        '#FF9800',
        '#FFB74D',
        '#FFE0B2'
      ];

      var yellows = [
        // '#FFFDE7',
        // '#FFF9C4',
        // '#FFF59D',
        // '#FFF176',
        // '#FFEE58',
        // '#FFEB3B',
        // '#FDD835',
        // '#FBC02D',
        // '#F9A825',
        // '#F57F17'
        '#F57F17',
        '#FBC02D',
        '#FFEB3B',
        '#FFF176',
        '#ffff99'
      ];

      var purples = [
        // '#F3E5F5',
        // '#E1BEE7',
        // '#CE93D8',
        // '#BA68C8',
        // '#AB47BC',
        // '#9C27B0',
        // '#8E24AA',
        // '#7B1FA2',
        // '#6A1B9A',
        // '#4A148C'
        '#4A148C',
        '#7B1FA2',
        '#9C27B0',
        '#BA68C8',
        '#E1BEE7'
      ];

      // NEED TO REVERSE IF USED
      var pinks = [
        // '#FCE4EC',
        // '#F8BBD0',
        // '#F48FB1',
        // '#F06292',
        // '#EC407A',
        // '#E91E63',
        // '#D81B60',
        // '#C2185B',
        // '#AD1457',
        // '#880E4F'
      ];

      var teals = [
        // '#E0F2F1',
        // '#B2DFDB',
        // '#80CBC4',
        // '#4DB6AC',
        // '#26A69A',
        // '#009688',
        // '#00897B',
        // '#00796B',
        // '#00695C',
        // '#004D40'
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