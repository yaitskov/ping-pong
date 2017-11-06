import angular from 'angular';
import template from './tr-parameters-editor.template.html';

angular.
    module('tournament').
    component('trParametersEditor', {
        templateUrl: template,
        controller: ['mainMenu', '$scope', 'Tournament', 'requestStatus',
                     '$routeParams', '$rootScope', 'binder',
                     function (mainMenu, $scope, Tournament, requestStatus,
                               $routeParams, $rootScope, binder) {
                         mainMenu.setTitle('Tournament Modification');
                         var self = this;
                         binder($scope, {
                             'event.tournament.rules.update': function (event, rules) {
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
                             },
                             'event.tournament.rules.cancel': function (event, rules) {
                                 window.history.back();
                             },
                             'event.tournament.rules.ready': function () {
                                 requestStatus.startLoading();
                                 Tournament.parameters(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (rules) {
                                         requestStatus.complete();
                                         $rootScope.$broadcast('event.tournament.rules.set', rules.toJSON());
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }]});
