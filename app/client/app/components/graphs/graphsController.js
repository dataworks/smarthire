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

			if( id == 'Language'){
				var chart = new Chart(ctx, {
					type: 'pie',
					data: {
						labels: labels,
						datasets: [{
							label: '# of Votes',
							data: count,
							backgroundColor: reds,
							borderColor: reds,
							borderWidth: 1
						}]
					}
				});
			}

			if( id == 'ETL'){
				var chart = new Chart(ctx, {
					type: 'pie',
					data: {
						labels: labels,
						datasets: [{
							label: '# of Votes',
							data: count,
							backgroundColor: oranges,
							borderColor: oranges,
							borderWidth: 1
						}]
					}
				});
			}

			if( id == 'Web'){
				var chart = new Chart(ctx, {
					type: 'pie',
					data: {
						labels: labels,
						datasets: [{
							label: '# of Votes',
							data: count,
							backgroundColor: yellows,
							borderColor: yellows,
							borderWidth: 1
						}]
					}
				});
			}

			if( id == 'Mobile'){
				var chart = new Chart(ctx, {
					type: 'pie',
					data: {
						labels: labels,
						datasets: [{
							label: '# of Votes',
							data: count,
							backgroundColor: greens,
							borderColor: greens,
							borderWidth: 1
						}]
					}
				});
			}

			if( id == 'Databases'){
				var chart = new Chart(ctx, {
					type: 'pie',
					data: {
						labels: labels,
						datasets: [{
							label: '# of Votes',
							data: count,
							backgroundColor: blues,
							borderColor: blues,
							borderWidth: 1
						}]
					}
				});
			}

			if( id == 'Big'){
				var chart = new Chart(ctx, {
					type: 'pie',
					data: {
						labels: labels,
						datasets: [{
							label: '# of Votes',
							data: count,
							backgroundColor: purples,
							borderColor: purples,
							borderWidth: 1
						}]
					}
				});
			}

		}
	}
]);
