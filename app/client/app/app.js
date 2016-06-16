var applicantApp = angular.module('applicantApp', [
  'ngRoute',
  'applicantControllers',
  'applicantServices',
  'infinite-scroll',
]);

var applicantControllers = angular.module('applicantControllers', []);
var applicantServices = angular.module('applicantServices', ['ngResource']);

applicantApp.config(['$routeProvider', '$locationProvider',
  function($routeProvider, $locationProvider) {
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
      when('/favorites', {
        templateUrl: 'app/components/applicants/applicantView.html',
        controller: 'ApplicantCtrl'
      }).
      when('/archive', {
        templateUrl: 'app/components/applicants/applicantView.html',
        controller: 'ApplicantCtrl'
      }).
      when('/review', {
        templateUrl: 'app/components/applicants/applicantView.html',
        controller: 'ApplicantCtrl'
      }).
      otherwise({
        redirectTo: '/applicants'
      });

    $locationProvider.html5Mode(true);
  }]);
