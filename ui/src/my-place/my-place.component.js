'use strict';

angular.
    module('myPlace').
    component('myPlace', {
        templateUrl: 'my-place/my-place.template.html',
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth) {
                         mainMenu.setTitle('My place');
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = {};
                         var self = this;
                         requestStatus.startLoading();
                         Place.aPlace({placeId: $routeParams.placeId},
                                      function (place) {
                                          requestStatus.complete();
                                          self.place = place;
                                          pageCtx.put('place', place);
                                          mainMenu.setTitle(place.name + " management");
                                          if (auth.isAuthenticated()) {
                                              mainMenu.setContextMenu({'#!/tournament/new': 'Add Tournament'});
                                          }
                                      },
                                      requestStatus.failed);
                     }]});
