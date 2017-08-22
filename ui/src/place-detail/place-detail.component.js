import angular from 'angular';
import template from './place-detail.template.html';

angular.
    module('placeDetail').
    component('placeDetail', {
        templateUrl: template,
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth', '$translate',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth, $translate) {
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = {};
                         var self = this;
                         requestStatus.startLoading();
                         $translate(['PlaceTitle', 'AddTournament']).then(function (translations) {
                             mainMenu.setTitle(transtions.PlaceTitle);
                             Place.aPlace({placeId: $routeParams.placeId},
                                          function (place) {
                                              requestStatus.complete();
                                              self.place = place;
                                              pageCtx.put('place', place);
                                              $translate('PlaceWithName', {name: place.name}).then(function (title) {
                                                  mainMenu.setTitle(title);
                                                  if (auth.isAuthenticated()) {
                                                      mainMenu.setContextMenu({'#!/tournament/new': translations.AddTournament});
                                                  }
                                              }
                                          },
                                          requestStatus.failed);
                         });
                     }]});
