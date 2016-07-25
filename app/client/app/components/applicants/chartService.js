applicantServices.factory('chartService', function() {
  return {
    /**
     * Stores the aggregation data into arrays & calls function to display charts
     * 
     * @param data - aggregation data
     * @param id - div id of the chart
     * @param return - returns instance of a pie chart
     */
    displayGraph: function (data, id, map) {
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


      if (id === 'Language') {
        return(this.createPieChart(ctx, labels, count, reds));
      }

      if (id === 'Web') {
        return(this.createPieChart(ctx, labels, count, oranges));
      }

      if (id === 'ETL') {
        return(this.createPieChart(ctx, labels, count, yellows));
      }

      if (id === 'Mobile') {
        return(this.createPieChart(ctx, labels, count, greens));
      }

      if (id === 'Databases') {
        return(this.createPieChart(ctx, labels, count, blues));
      }

      if (id === 'Big') {
        return(this.createPieChart(ctx, labels, count, purples));
      }
    },

    /**
     * Creates a pie chart using the chart.js library
     *
     * @param ctx - id of the chart
     * @param labels - terms
     * @param count - number of occurances
     * @param color - color scheme of the chart
     * @return chart - 
     */
    createPieChart: function (ctx, labels, count, color) {
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
	  
	   return chart;
    },

    /**
     * creates a bar graph for an applicant's scoring breakdown
     * @param applicant - id of applicant 
     * @return bar - final graph that will show up in the applicant table
     */
    createScoreChart: function(applicant) {
      var keys = [];
      var values = [];
      var finalValues = [];

      //sort from least to greatest, switch a & b for opposite
      var keysSorted = Object.keys(applicant.features).sort(function(a,b) {
        return applicant.features[b]-applicant.features[a]});


      for(var key in keysSorted) {
        keys.push(keysSorted[key]);
        values.push((parseFloat(applicant.features[keysSorted[key]])).toFixed(2));
      }

      finalValues.push(values);

      var data = {
        labels: keys,
        series: finalValues,
      };

      var options = {
        high: 5,
        low: -5,
        width: 900,
        height: 275,
      };

      var responsiveOptions = [
      // ipad
        ['screen and (min-width: 768px) and (max-width: 991px)', {
          width: 760,
          height: 175,
          axisX: {
            labelInterpolationFnc: function (value) {
              return value;
            }
          }
        }],
        // phone
        ['screen and (max-width: 767px)', {
          width: 420,
          height: 175,
          axisX: {
            labelInterpolationFnc: function (value) {
              return value.substring(0,3);
            }
          }
        }]
      ];

      var bar = new Chartist.Bar("#chart-" + applicant.id, data, options, responsiveOptions);

      return bar;
    }
  }
});