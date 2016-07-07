applicantControllers.controller('ApplicantCtrl', ['$scope', '$location', 'Analysis', 'Applicant', 'Label', 'Suggest', 'Upload', '$window', 'ngToast', '$timeout', 'advancedSearch',
  function($scope, $location, analysis, Applicant, Label, Suggest, Upload, $window, ngToast, $timeout, advancedSearch) {

    //default dropdown menu to 'new' on page load
    $scope.selection = "new";
    $scope.sort = "score";
    $scope.sortOrder = "desc";

    //query should start off at index 0, displaying first item
    $scope.index = 0;
    //displaying 25 applicants at a time
    $scope.pageSize = 25;
    $scope.loadingData = false;
    $scope.hasData = true;

    //sorting table by column code
    $scope.propertyName = null;
    $scope.reverse = false;
    $scope.searchText = "";
    $scope.displayText = "";

    // for expand button
    $scope.active = true;

    //for search analytics
    $scope.showGraphs = false;

    $scope.queries = [$scope.languages, $scope.etl, $scope.web,
      $scope.mobile, $scope.db, $scope.bigData
    ];

    var fields = ['languages', 'etl', 'web', 'mobile', 'db', 'bigData'];
    var ids = ['Language', 'ETL', 'Web', 'Mobile', 'Databases', 'Big'];
    var charts = [];

    $('.openall').click(function(){
      if ($scope.active) {
        $scope.active = false;
        $('.accordian-body:not(".in")').collapse('show');
        $(this).find('i').toggleClass('glyphicon glyphicon-plus-sign').toggleClass('glyphicon glyphicon-minus-sign');
      } 
      else {
        $scope.active = true;
        $('.accordian-body.in').collapse('hide');
        $(this).find('i').toggleClass('glyphicon glyphicon-minus-sign').toggleClass('glyphicon glyphicon-plus-sign');
      }
    });

    /**
     * function that changes the suggestion query based on the text that is in the search bar
     *
     * @param text- text that is currently in the search bar
     */

    $scope.autoComplete = function(text) {
      $scope.searchText = text;
      $scope.autoSuggest = Suggest.query({
        term: $scope.searchText
      });
    }

    //alternative to ng-change, calls $scope.autoComplete when 'searchText' has been modified
    $scope.$watch('displayText', $scope.autoComplete);

    //default query
    $scope.applicants = Applicant.query({
      from: $scope.index,
      size: $scope.pageSize,
      sort: $scope.sort,
      order: $scope.sortOrder
    });

    $scope.setGraphBool = function() {
      if ($scope.showGraphs == false) {
        $scope.showGraphs = true;
      } else {
        $scope.showGraphs = false;
      }
    }

    /**
     * sort by property name. function is called when column is clicked
     *
     * @param type- type to sort by (i.e. Score)
     */

    $scope.sortColumn = function(type) {
      $scope.index = 0;
      $scope.hasData = true;
      $scope.sort = type;

      if (type == "score") {
        $scope.sortBool = false;
      } else {
        $scope.sortBool = true;
      }

      if ($scope.sortOrder == "asc") {
        $scope.sortOrder = "desc";
        $scope.reverse = false;
      } 

      else if ($scope.sortOrder == "desc") {
        $scope.sortOrder = "asc";
        $scope.reverse = true;
      }

      $scope.applicants = Applicant.query({
        type: $scope.selection,
        from: $scope.index,
        size: $scope.pageSize,
        sort: $scope.sort,
        order: $scope.sortOrder,
        query: $scope.searchText
      });
    }

    /**
     * change queries when new type is selected from the dropdown menu
     *
     * @param type- select box value
     */
    $scope.showSelectValue = function(type) {
      $scope.searchText = "";
      $scope.displayText = "";
      $scope.index = 0;
      $scope.hasData = true;
      $scope.selection = type;
      $scope.applicants = Applicant.query({
        type: type,
        from: $scope.index,
        size: $scope.pageSize,
        sort: $scope.sort,
        order: $scope.sortOrder
      });
    };

    /**
     * adds to query if there is more data, else change hasData to false
     *
     * @param result- rest of the query
     */
    $scope.dataLoaded = function(result) {
      var rows = result.rows;

      if (rows.length > 0) {
        if ($scope.applicants.rows == null) {
          $scope.applicants.rows = rows;
        } else {
          $scope.applicants.rows = $scope.applicants.rows.concat(rows);
        }
      } else {
        $scope.hasData = false;
        $scope.index = 0;
      }

      $scope.loadingData = false;
    };

    /**
     * check if there is more data to load from the query, for infinite scroll
     *
     */
    $scope.nextPage = function() {
      if ($scope.hasData) {
        $scope.loadingData = true;
        $scope.index += $scope.pageSize;
        
        Applicant.query({
          query: $scope.searchText,
          type: $scope.selection,
          from: $scope.index,
          size: $scope.pageSize,
          order: $scope.sortOrder,
          sort: $scope.sort
        }, $scope.dataLoaded);
      }
    };

    // Only enable if the document has a long scroll bar
    // Note the window height + offset
    $('#top-link-block').removeClass('hidden').affix({
      offset: {
        top: 100
      }
    });

    /**
     * function that is called when action button is clicked, i.e. Favorite, Archive, Review
     *
     * @param id- id number of the applicant
     * @param type- type of the applicant
     * @param applicant- applicant object itself, passed in to avoid wrong indexing
     */
    $scope.mark = function(id, type, applicant) {
      var label = new Label({
        'id': id,
        'type': type
      });
      label.$save().then(function() {
        $scope.applicants.rows.splice($scope.applicants.rows.indexOf(applicant), 1);
      });
    }

    /**
     * function that is called when applicant is placed back in 'New
     *
     * @param id- id number of the applicant
     * @param applicant- applicant object itself, passed in to avoid wrong indexing
     */
    $scope.remove = function(id, applicant) {
      Label.delete({
        'id': id
      }).$promise.then(function() {
        $scope.applicants.rows.splice($scope.applicants.rows.indexOf(applicant), 1);
      });
    }

    /** 
     * change toast CSS to show the message
     * after about three seconds, hide the toast
     *
     * @param type- type of toast to show
     *
     */
    $scope.showToast = function(type) {
      if (type == 'Favorite') {
        ngToast.create("Applicant added to Favorites");
      }

      else if (type == 'Review') {
        ngToast.create({
          className: 'warning',
          content: 'Applicant added to Review'
        });

      }

      else if (type == 'Archive') {
        ngToast.create({
          className: 'danger',
          content: 'Applicant added to Archive'
        });

      }

      else if (type == 'Upload') {
        ngToast.create("Resume has been Uploaded");
      }

      else {
        ngToast.create({
          className: 'info',
          content: 'Applicant sent back to home page'
        });

      }
    }

    /** 
     * return image from a link
     *
     * @param id- id of applicant
     * @param type-image
     *
     */

    $scope.getLink = function(id, type) {
      return "service/attachments?id=" + id + "&type=" + type;
    }

    /** 
     * return query based on text that was input in search bar
     *
     * @param searchText- text that was input in search bar
     *
     */
    $scope.search = function(searchText) {
      $scope.index = 0;
      $scope.searchText = searchText;


      //sets a boolean based on which checkboxes are checked
      var csChecked = document.getElementById("csCheck").checked;
      var cpeChecked = document.getElementById("cpeCheck").checked;
      var itChecked = document.getElementById("itCheck").checked;
      var vaChecked = document.getElementById("vaCheck").checked;
      var mdChecked = document.getElementById("mdCheck").checked;
      var dcChecked = document.getElementById("dcCheck").checked;
      var uvaChecked = document.getElementById("uvaCheck").checked;
      var jmuChecked = document.getElementById("jmuCheck").checked;
      var rpiChecked = document.getElementById("rpiCheck").checked;
      var devChecked = document.getElementById("devCheck").checked;
      var arcChecked = document.getElementById("arcCheck").checked;
      var manChecked = document.getElementById("manCheck").checked;
      
      //calls the createQuery function in searchService.js
      $scope.searchText = $scope.searchText + advancedSearch.createQuery(csChecked, cpeChecked, itChecked, vaChecked, mdChecked, dcChecked, uvaChecked, jmuChecked, rpiChecked, devChecked, arcChecked, manChecked);

      $scope.applicants = Applicant.query({
        query: $scope.searchText,
        from: $scope.index,
        size: $scope.pageSize,
        sort: $scope.sort,
        order: $scope.sortOrder
      });

    for(var i = 0; i < charts.length; i++)
      charts[i].destroy();

    $scope.queries.forEach(function(value, index) {
      $scope.queries[index] = analysis.query({
        query: $scope.searchText,
        field: fields[index]
      });

      $scope.queries[index].$promise.then(function(data) {
        displayGraph(data, ids[index]);
      });
    });
      //sets text in search bar to what user typed in, hides the query call
      $scope.displayText = searchText;
    }

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
        '#1565C0',
        '#1976D2',
        '#1E88E5',
        '#2196F3',
        '#42A5F5',
        '#64B5F6',
        '#90CAF9',
        '#BBDEFB',
        '#BBDEFB',
        '#E3F2FD'
      ];

      var reds = [
        '#B71C1C',
        '#FF1919',
        '#FF3232',
        '#FF4C4C',
        '#FF6666',
        '#FF7F7F',
        '#FF9999',
        '#FFB2B2',
        '#FFCCCC',
        '#FFE5E5'
      ];

      var greens = [
        '#1B5E20',
        '#2E7D32',
        '#388E3C',
        '#43A047',
        '#4CAF50',
        '#66BB6A',
        '#81C784',
        '#A5D6A7',
        '#C8E6C9',
        '#E8F5E9'
      ];

      var oranges = [
        '#E65100',
        '#EF6C00',
        '#F57C00',
        '#FB8C00',
        '#FF9800',
        '#FFA726',
        '#FFB74D',
        '#FFCC80',
        '#FFE0B2',
        '#FFF3E0'
      ];

      var yellows = [
        '#F57F17',
        '#F9A825',
        '#FBC02D',
        '#FDD835',
        '#FFEB3B',
        '#FFEE58',
        '#FFF176',
        '#FFF59D',
        '#ffff99',
        '#ffffb2'
      ];

      var purples = [
        '#4A148C',
        '#6A1B9A',
        '#7B1FA2',
        '#8E24AA',
        '#9C27B0',
        '#AB47BC',
        '#BA68C8',
        '#CE93D8',
        '#E1BEE7',
        '#F3E5F5'
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
      charts.push(chart);
    }

    /** 
     * Converts user uploaded files to base64 strings and indexes them 
     *
     */
    $scope.upload = function() {
      var files = document.querySelector('input[type=file]').files;
      for(var i = 0; i < files.length; i++) {
        (function(file) {
          var reader = new FileReader();

          reader.addEventListener("load", function () {
            var temp = reader.result.split(',');
            var base64string = temp[1];
            var hash = calcMD5(base64string);
            
            var upload = new Upload({
              'id': hash,
              'type': 'upload',
              'base64string': base64string,
              'name': file.name,
              'processed': false
            });

            upload.$save();
          }, false);

          if(file)
            reader.readAsDataURL(file);
        })(files[i]);
      }
      $scope.showToast('Upload');

    }

    //scroll code
    $(function() {
      var lastScrollTop = 0,
        delta = 5;
      $(window).scroll(function(event) {
        var st = $(this).scrollTop();

        if (Math.abs(lastScrollTop - st) <= delta)
          return;

        //if scrolling up, hide the footer
        if (st <= lastScrollTop) {;
          angular.element("#footer").hide();
        }

        lastScrollTop = st;
      });
    });

    //if at bottom of window, show footer 
    $(window).scroll(function() {
      if ($(window).scrollTop() + $(window).height() == $(document).height()) {
        angular.element("#footer").show();
      } else {
        angular.element("#footer").hide();
      }
    });

  }]);


/**
*
* A custom directive to bind file upload
* 
* @param: The attributes name
* @param: A callback function which binds the upload function to the attribute
*/
applicantControllers.directive('customOnChange', function() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      var onChangeFunc = scope.$eval(attrs.customOnChange);
      element.bind('change', onChangeFunc);
    }
  };
})
