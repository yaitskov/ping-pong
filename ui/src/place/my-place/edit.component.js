import angular from 'angular';
import template from './edit.template.html';

angular.
    module('place').
    component('myPlaceEdit', {
        templateUrl: template,
        controller: [
            'mainMenu', 'auth', 'requestStatus', 'Place', '$routeParams', 'binder', '$scope',
            function (mainMenu, auth, requestStatus, Place, $routeParams, binder, $scope) {
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
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Place Modification'),
                    'event.request.status.ready': (event) => {
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
                                         mainMenu.setTitle(['ModificationOf', {name: place.name}]);
                                     },
                                     requestStatus.failed);
                    }
                });
            }]});
