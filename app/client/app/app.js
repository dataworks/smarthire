var applicantApp = angular.module('applicantApp', [
  'ngRoute',
  'applicantControllers',
  'applicantServices',
]);

var applicantControllers = angular.module('applicantControllers', []);
var applicantServices = angular.module('applicantServices', ['ngResource']);

applicantApp.config(['$routeProvider', '$locationProvider',
  function($routeProvider, $locationProvider) {
    $routeProvider.
      when('/service/applicants', {
        templateUrl: 'app/components/applicants/applicantView.html',
        controller: 'ApplicantCtrl'
      }).
      when('/service/search', {
        templateUrl: 'app/components/search/searchView.html',
        controller: 'SearchCtrl'
      }).
      when('/service/explore', {
        templateUrl: 'app/components/explore/exploreView.html',
        controller: 'ExploreCtrl'
      }).
      when('/service/about', {
        templateUrl: 'app/components/about/aboutView.html',
        controller: 'AboutCtrl'
      }).
      when('/service/contact', {
        templateUrl: 'app/components/contact/contactView.html',
        controller: 'ContactCtrl'
      }).
      otherwise({
        redirectTo: '/service/applicants'
      });

    $locationProvider.html5Mode(true);
  }]);
