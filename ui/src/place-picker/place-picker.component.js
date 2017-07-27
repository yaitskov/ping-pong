'use strict';

angular.module('placePicker').
    component('placePicker', {
        templateUrl: 'place-picker/place-picker.template.html',
        controller: ['placePicker', 'mainMenu', 'requestStatus',
                     function (placePicker, mainMenu, requestStatus) {
                         mainMenu.setTitle('Choose place');
                         mainMenu.setContextMenu({'#!/my/new/place': 'Add Place'});
                         this.places = [];
                         var self = this;
                         this.pickThisOne = function (i) {
                             placePicker.setChosen(self.places[i]);
                         }
                         requestStatus.startLoading();
                         placePicker.getList(
                             function (l) {
                                 requestStatus.complete();
                                 self.places = l;
                             },
                             requestStatus.failed);
                     }
                    ]
    });
