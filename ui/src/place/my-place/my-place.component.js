import angular from 'angular';
import template from './my-place.template.html';

angular.
    module('place').
    component('myPlace', {
        templateUrl: template,
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth', 'binder', '$scope',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth, binder, $scope) {
                         var extraMenu = auth.isAuthenticated() ? {'#!/tournament/new': 'AddTournament'} : null;
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = null;
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('My place', extraMenu),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Place.aPlace({placeId: $routeParams.placeId},
                                              function (place) {
                                                  requestStatus.complete();
                                                  self.place = place;
                                                  pageCtx.put('place', place);
                                                  mainMenu.setTitle(['ManagementOf', {name: place.name}], extraMenu);
                                              },
                                            (...a) => requestStatus.failed(...a));
                             }
                         });
                     }]});
