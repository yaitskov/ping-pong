import angular from 'angular';
import template from './place-list.template.html';

angular.module('place').
    component('placeList', {
        templateUrl: template,
        controller: ['Place', 'mainMenu', 'requestStatus', 'binder', '$scope',
                     function (Place, mainMenu, requestStatus, binder, $scope) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('My Places', {'#!/my/new/place': 'AddPlaceBtn'}),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Place.myPlaces(
                                     {},
                                     function (places) {
                                         requestStatus.complete();
                                         self.places = places;
                                     },
                                   (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
    });
