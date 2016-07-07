var applicantApp = angular.module('applicantApp', [
  'ngRoute',
  'applicantControllers',
  'applicantServices',
  'infinite-scroll',
  'ngSanitize',
  'ngToast',
  'ngAnimate',
  'autocomplete',
]);

var applicantControllers = angular.module('applicantControllers', []);
var applicantServices = angular.module('applicantServices', ['ngResource']);

applicantApp.config(['$routeProvider', '$locationProvider', 'ngToastProvider',
  function($routeProvider, $locationProvider, ngToastProvider) {
    $routeProvider.
    when('/applicants', {
      templateUrl: 'app/components/applicants/applicantView.html',
      controller: 'ApplicantCtrl'
    }).
    when('/about', {
      templateUrl: 'app/components/about/aboutView.html',
      controller: 'AboutCtrl'
    }).
    when('/analysis', {
      templateUrl: 'app/components/analysis/analysisView.html',
      controller: 'AnalysisCtrl'
    }).
    otherwise({
      redirectTo: '/applicants'
    });

    //sets html5Mode to avoid having '#' in URLs
    $locationProvider.html5Mode(true);

    //give toast animations
    ngToastProvider.configure({
      animation: 'fade',
      verticalPosition: 'top',
      horizontalPosition: 'left'
    });
  }
]);