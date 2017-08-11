'use strict';

angular.
    module('newPlace').
    component('newPlace', {
        templateUrl: 'new-place/new-place.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location', 'requestStatus',
                     'City', 'Country', 'LocalStorage', '$timeout',
                     function (auth, mainMenu, $http, $location, requestStatus,
                               City, Country, LocalStorage, $timeout) {
                         mainMenu.setTitle('New Place');
                         this.form = {};
                         this.cityForm = {};
                         this.countryForm = {};
                         this.place = {address: {city: {id: null}}};
                         var self = this;
                         this.countryId = LocalStorage.get('myCountryId');
                         this.countries = null;
                         this.cities = [];
                         this.cityName = null;
                         this.countryName = null;
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
                                     requestStatus.failed);
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
                                     requestStatus.failed);
                         };
                         this.createPlace = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Creating');
                             $http.post('/api/place/create',
                                        self.place,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         requestStatus.complete();
                                         window.history.back();
                                     },
                                     requestStatus.failed);
                         };
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
                             requestStatus.failed);
                         this.loadCities = function () {
                             requestStatus.startLoading('Cities');
                             City.list(
                                 {countryId: self.countryId},
                                 function (cities) {
                                     self.cities = cities;
                                     requestStatus.complete();
                                 },
                                 requestStatus.failed);
                         }
                         if (self.countryId) {
                             this.loadCities();
                         }
                     }]});
