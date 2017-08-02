'use strict';

angular.
    module('myPlaceEdit').
    component('myPlaceEdit', {
        templateUrl: 'my-place/edit.template.html',
        controller: [
            'mainMenu', 'auth', 'requestStatus', 'Place', '$routeParams',
            function (mainMenu, auth, requestStatus, Place, $routeParams) {
                mainMenu.setTitle('Place Modification');
                var self = this;
                this.place = null;
                this.data = {address: {}};
                this.change = function () {
                    self.form.$setSubmitted();
                    if (!self.form.$valid) {
                        return;
                    }
                    requestStatus.startLoading("Saving");
                    Place.change(self.data,
                                function (ok) {
                                    requestStatus.complete();
                                    history.back();
                                },
                                requestStatus.failed);
                };
                requestStatus.startLoading();
                Place.aPlace({placeId: $routeParams.placeId},
                             function (place) {
                                 requestStatus.complete();
                                 self.place = place;
                                 self.data.pid = $routeParams.placeId;
                                 self.data.name = self.place.name;
                                 self.data.address.email = self.place.address.email;
                                 self.data.address.phone = self.place.address.phone;
                                 self.data.address.address = self.place.address.address;
                                 mainMenu.setTitle(place.name + " modification");
                             },
                             requestStatus.failed);
            }]});