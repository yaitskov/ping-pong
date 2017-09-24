import angular from 'angular';
import template from './parameters.template.html';

angular.
    module('tournamentParameters').
    component('tournamentParameters', {
        templateUrl: template,
        controller: ['mainMenu', '$scope', 'Tournament', 'requestStatus',
                     '$routeParams', '$rootScope', 'lateEvent',
                     function (mainMenu, $scope, Tournament, requestStatus,
                               $routeParams, $rootScope, lateEvent) {
                         mainMenu.setTitle('Tournament Modification');
                         var self = this;
                         var unbindUpdateEvent = $rootScope.$on('event.tournament.rules.update', function (event, rules) {
                             requestStatus.startLoading('Saving changes');
                             Tournament.updateParams(
                                 {tid: $routeParams.tournamentId, rules: rules},
                                 function (ok) {
                                     requestStatus.complete();
                                     history.back();
                                 },
                                 function (resp) {
                                     if (resp.data && resp.data.field2Errors) {
                                         $rootScope.$broadcast('event.tournament.rules.errors',
                                                               resp.data.field2Errors);
                                     }
                                     requestStatus.failed(resp);
                                 });
                         });
                         var unbindCancelEvent = $rootScope.$on('event.tournament.rules.cancel', function (event, rules) {
                             window.history.back();
                         });
                         $scope.$on('$destroy', function() {
                             unbindUpdateEvent();
                             unbindCancelEvent();
                         });
                         lateEvent(
                             function () {
                                 requestStatus.startLoading();
                                 Tournament.parameters(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (rules) {
                                         requestStatus.complete();
                                         $rootScope.$broadcast('event.tournament.rules.set', rules.toJSON());
                                     },
                                     requestStatus.failed);
                             });
                     }]});
