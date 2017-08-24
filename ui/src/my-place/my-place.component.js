import angular from 'angular';
import template from './my-place.template.html';

angular.
    module('myPlace').
    component('myPlace', {
        templateUrl: template,
        controller: ['mainMenu', 'Place', '$routeParams', 'requestStatus', 'pageCtx', 'auth',
                     function (mainMenu, Place, $routeParams, requestStatus, pageCtx, auth) {
                         mainMenu.setTitle('My place');
                         pageCtx.put('place', {pid: $routeParams.placeId});
                         this.place = null;
                         var self = this;
                         requestStatus.startLoading();
                         Place.aPlace({placeId: $routeParams.placeId},
                                      function (place) {
                                          requestStatus.complete();
                                          self.place = place;
                                          pageCtx.put('place', place);
                                          mainMenu.setTitle(['ManagementOf', {name: place.name}],
                                                            auth.isAuthenticated() ? {'#!/tournament/new': 'AddTournament'} : null);
                                      },
                                      requestStatus.failed);
                     }]});
