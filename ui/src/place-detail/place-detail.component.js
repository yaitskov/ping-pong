'use strict';

angular.
    module('placeDetail').
    component('placeDetail', {
        templateUrl: 'place-detail/place-detail.template.html',
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth) {
                         mainMenu.setTitle('Place');
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = {};
                         var self = this;
                         requestStatus.startLoading();
                         Place.aPlace({placeId: $routeParams.placeId},
                                      function (place) {
                                          requestStatus.complete();
                                          self.place = place;
                                          pageCtx.put('place', place);
                                          mainMenu.setTitle('Place ' + place.name);
                                          if (auth.isAuthenticated()) {
                                              mainMenu.setContextMenu({'#!/tournament/new': 'Add Tournament'});
                                          }
                                      },
                                      requestStatus.failed);
                     }]});
