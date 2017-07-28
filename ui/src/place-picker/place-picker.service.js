'use strict';

angular.module('placePicker').
    factory('placePicker', ['$location', 'Place', function ($location, Place) {
        return new function () {
            console.log("reload places2");
            this.chosenPlace = null;
            this.getList = function (cb, failed) {
                Place.myPlaces({}, cb, failed);
            };
            this.pickFrom = function () {
                $location.path('/pick/place');
            };
            this.setChosen = function (place) {
                this.chosenPlace = place;
                window.history.back();
            }
            this.reset = function () {
                self.chosenPlace = null;
            };
            this.getChosenPlace = function () {
                return this.chosenPlace;
            };
        };
    }]);
