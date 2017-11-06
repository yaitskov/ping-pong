import angular from 'angular';
import template from './place-picker.template.html';

angular.module('placePicker').
    component('placePicker', {
        templateUrl: template,
        controller: ['placePicker', 'mainMenu', 'requestStatus', 'binder', '$scope',
                     function (placePicker, mainMenu, requestStatus, binder, $scope) {
                         this.places = [];
                         var self = this;
                         this.pickThisOne = function (i) {
                             placePicker.setChosen(self.places[i]);
                         }
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('ChoosePlace', {'#!/my/new/place': 'AddPlaceBtn'}),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 placePicker.getList(
                                     function (l) {
                                         requestStatus.complete();
                                         self.places = l;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
    });
