import angular from 'angular';
import template from './place-picker.template.html';

angular.module('placePicker').
    component('placePicker', {
        templateUrl: template,
        controller: ['placePicker', 'mainMenu', 'requestStatus', '$translate',
                     function (placePicker, mainMenu, requestStatus, $translate) {
                         $translate(['ChoosePlace', 'AddPlaceBtn']).then(function (translations) {
                             mainMenu.setTitle(translations.ChoosePlace);
                             mainMenu.setContextMenu({'#!/my/new/place': translations.AddPlaceBtn});
                         });
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
