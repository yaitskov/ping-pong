import angular from 'angular';
import template from './place-detail.template.html';

angular.
    module('placeDetail').
    component('placeDetail', {
        templateUrl: template,
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth) {
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = {};
                         var self = this;
                         requestStatus.startLoading();
                         mainMenu.setTitle('PlaceTitle');
                         Place.aPlace({placeId: $routeParams.placeId},
                                      function (place) {
                                          requestStatus.complete();
                                          self.place = place;
                                          pageCtx.put('place', place);
                                          mainMenu.setTitle(['PlaceWithName', {name: place.name}],
                                                                auth.isAuthenticated() ? {'#!/tournament/new':  'AddTournament'} : null);
                                      },
                                      requestStatus.failed);
                     }]});
