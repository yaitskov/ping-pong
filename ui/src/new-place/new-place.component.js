'use strict';

angular.
    module('newPlace').
    component('newPlace', {
        templateUrl: 'new-place/new-place.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location', 'requestStatus',
                     function (auth, mainMenu, $http, $location, requestStatus) {
                         mainMenu.setTitle('New Place');
                         this.place = {};
                         var self = this;
                         this.createPlace = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Creating');
                             $http.post('/api/place/create',
                                        self.place,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         requestStatus.complete();
                                         window.history.back();
                                     },
                                     requestStatus.failed);
                         };
                     }]});
