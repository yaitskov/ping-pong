'use strict';

angular.module('placeList').
    component('placeList', {
        templateUrl: 'place-list/place-list.template.html',
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
