'use strict';

angular.module('placeList').
    component('placeList', {
        templateUrl: 'place-list/place-list.template.html',
        controller: ['Place', 'mainMenu',
                     function (Place, mainMenu) {
                         mainMenu.setTitle('My Places');
                         mainMenu.setContextMenu({'#!/my/new/place': 'Add Place'});
                         this.places = Place.myPlaces();
                     }
                    ]
    });
