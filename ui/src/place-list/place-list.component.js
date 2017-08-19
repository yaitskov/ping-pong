import angular from 'angular';
import template from './place-list.template.html';

angular.module('placeList').
    component('placeList', {
        templateUrl: template,
        controller: ['Place', 'mainMenu', 'requestStatus',
                     function (Place, mainMenu, requestStatus) {
                         mainMenu.setTitle('My Places');
                         mainMenu.setContextMenu({'#!/my/new/place': 'Add Place'});
                         this.places = null;
                         var self = this;
                         requestStatus.startLoading();
                         Place.myPlaces(
                             {},
                             function (places) {
                                 requestStatus.complete();
                                 self.places = places;
                             },
                             requestStatus.failed);
                     }
                    ]
    });
