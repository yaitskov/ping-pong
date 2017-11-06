import angular from 'angular';
import template from './place-detail.template.html';

angular.
    module('place').
    component('placeDetail', {
        templateUrl: template,
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth', 'binder', '$scope',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth, binder, $scope) {
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = {};
                         var extraMenu = auth.isAuthenticated() ? {'#!/tournament/new':  'AddTournament'} : null;
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('PlaceTitle', extraMenu),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Place.aPlace({placeId: $routeParams.placeId},
                                              function (place) {
                                                  requestStatus.complete();
                                                  self.place = place;
                                                  pageCtx.put('place', place);
                                                  mainMenu.setTitle(['PlaceWithName', {name: place.name}], extraMenu);
                                              },
                                              requestStatus.failed);
                             }
                         });
                     }]});
