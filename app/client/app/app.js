var applicantApp = angular.module('applicantApp', [
  'ngRoute',
  'applicantControllers',
  'applicantServices',
]);

var applicantControllers = angular.module('applicantControllers', []);
var applicantServices = angular.module('applicantServices', ['ngResource']);

applicantApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/applicants', {
        templateUrl: 'app/components/applicants/applicantView.html',
        controller: 'ApplicantCtrl'
      }).
      when('/search', {
        templateUrl: 'app/components/search/searchView.html',
        controller: 'SearchCtrl'
      }).
      when('/explore', {
        templateUrl: 'app/components/explore/exploreView.html',
        controller: 'ExploreCtrl'
      }).
      when('/about', {
        templateUrl: 'app/components/about/aboutView.html',
        controller: 'AboutCtrl'
      }).
      when('/contact', {
        templateUrl: 'app/components/contact/contactView.html',
        controller: 'ContactCtrl'
      }).
      otherwise({
        redirectTo: '/applicants'
      });
  }]);
