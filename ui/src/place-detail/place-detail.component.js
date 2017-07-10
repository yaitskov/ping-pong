'use strict';

angular.
    module('placeDetail').
    component('placeDetail', {
        templateUrl: 'place-detail/place-detail.template.html',
        controller: ['mainMenu', 'Place', '$routeParams', 'pageCtx',
                     function (mainMenu, Place, $routeParams, pageCtx) {
                         mainMenu.setTitle('Place');
                         mainMenu.setContextMenu({'#!/tournament/new': 'Add Tournament'});
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = {};
                         var self = this;
                         self.error = null;
                         this.updatePlace = function () {
                             console.log("implement place update");
                         };
                         Place.aPlace({placeId: $routeParams.placeId},
                                      function (place) {
                                          self.place = place;
                                          pageCtx.put('place', place);
                                          mainMenu.setTitle('Place ' + place.name);
                                          mainMenu.setContextMenu({'#!/tournament/new': 'Add Tournament'});
                                      });
                     }]});
