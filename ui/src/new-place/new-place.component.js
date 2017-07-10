'use strict';

angular.
    module('newPlace').
    component('newPlace', {
        templateUrl: 'new-place/new-place.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location',
                     function (auth, mainMenu, $http, $location) {
                         mainMenu.setTitle('New Place');
                         this.place = {};
                         var self = this;
                         self.error = null;
                         this.createPlace = function () {
                             console.log("create place");
                             $http.post('/api/place/create',
                                        self.place,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         console.log("place created: " + okResp.data);
                                         window.history.back();
                                     },
                                     function (badResp) {
                                         self.error = "" + badResp;
                                     });
                         };
                     }]});
