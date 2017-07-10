'use strict';

angular.module('placePicker').
    component('placePicker', {
        templateUrl: 'place-picker/place-picker.template.html',
        controller: ['placePicker', 'mainMenu', '$scope',
                     function (placePicker, mainMenu) {
                         mainMenu.setTitle('Choose place');
                         mainMenu.setContextMenu({'#!/my/new/place': 'Add Place'});
                         this.places = [];
                         var self = this;
                         this.pickThisOne = function (i) {
                             placePicker.setChosen(self.places[i]);
                         }
                         placePicker.getList(function (l) { self.places = l; });
                     }
                    ]
    });
