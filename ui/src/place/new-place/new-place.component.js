import angular from 'angular';
import template from './new-place.template.html';

angular.
    module('place').
    component('newPlace', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', 'requestStatus',
                     'City', 'Country', 'LocalStorage', '$timeout', 'pageCtx', 'binder', '$scope',
                     function (auth, mainMenu, $http, $location, requestStatus,
                               City, Country, LocalStorage, $timeout, pageCtx, binder, $scope) {
                         this.form = {};
                         this.cityForm = {};
                         this.countryForm = {};
                         this.place = pageCtx.get('new-place') || {address: {city: {id: null}}};
                         var self = this;
                         this.countryId = LocalStorage.get('myCountryId');
                         this.countries = null;
                         this.cities = [];
                         this.cityName = null;
                         this.countryName = null;
                         this.safeBack = function () {
                             pageCtx.put('new-place', this.place);
                             window.history.back();
                         };
                         this.countryChange = function () {
                             if (self.countryId == 'addNewCountry') {
                                 self.cities = [];
                                 self.countryId = null;
                                 jQuery('#newCountryForm').modal('show');
                                 $timeout(function() {
                                     jQuery('#countryname').focus();
                                 }, 1000);
                             } else if (self.countryId) {
                                 LocalStorage.store('myCountryId', self.countryId);
                                 self.loadCities();
                             }
                         };
                         this.showNewCityForm = function () {
                             jQuery('#newCityForm').modal('show');
                             $timeout(function() {
                                 jQuery('#cityname').focus();
                             }, 1000);
                             self.place.address.city.id = null;
                         };
                         this.cityChange = function () {
                             if (self.place.address.city.id == 'addNewCity') {
                                 self.showNewCityForm();
                             }
                         }
                         this.createCityAndUse = function () {
                             self.cityForm.$setSubmitted();
                             if (!self.cityForm.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Creating city');
                             $http.post('/api/city/create',
                                        {countryId: self.countryId,
                                         name: self.cityName},
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (ok) {
                                         self.cities.push({id: ok.data, name: self.cityName});
                                         self.place.address.city.id = "" + ok.data;
                                         jQuery('#newCityForm').modal('hide');
                                         requestStatus.complete();
                                         self.cityForm.$setPristine(true);
                                     },
                                   (...a) => requestStatus.failed(...a));

                         };
                         this.createCountryAndUse = function () {
                             self.countryForm.$setSubmitted();
                             if (!self.countryForm.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Creating country');
                             $http.post('/api/country/create',
                                        {name: self.countryName},
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (ok) {
                                         self.countries.push({id: ok.data, name: self.countryName});
                                         self.countryId = "" + ok.data; // to string to match type
                                         jQuery('#newCountryForm').modal('hide');
                                         requestStatus.complete();
                                         self.countryForm.$setPristine(true);
                                         self.showNewCityForm();
                                     },
                                   (...a) => requestStatus.failed(...a));
                         };
                         this.createPlace = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Creating place');
                             $http.post('/api/place/create',
                                        self.place,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         pageCtx.put('new-place', self.place);
                                         requestStatus.complete();
                                         window.history.back();
                                     },
                                   (...a) => requestStatus.failed(...a));
                         };
                         self.loadCities = function () {
                             requestStatus.startLoading('Cities');
                             City.list(
                                 {countryId: self.countryId},
                                 function (cities) {
                                     self.cities = cities;
                                     requestStatus.complete();
                                 },
                               (...a) => requestStatus.failed(...a));
                         }
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('New Place'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading('Countries');
                                 Country.list(
                                     {},
                                     function (countries) {
                                         self.countries = countries;
                                         if (!self.countryId) {
                                             for (var i in countries) {
                                                 self.countryId = countries[i].id;
                                                 break;
                                             }
                                             self.loadCities();
                                         }
                                         requestStatus.complete();
                                     },
                                   (...a) => requestStatus.failed(...a));

                                 if (self.countryId) {
                                     self.loadCities();
                                 }
                             }
                         });
                     }]});
