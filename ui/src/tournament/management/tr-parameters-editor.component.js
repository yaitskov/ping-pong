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
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Tournament Modification'),
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
                             'event.tournament.rules.cancel': window.history.back,
                             'event.tournament.rules.ready': () => {
                                 requestStatus.startLoading();
                                 const req = {tournamentId: $routeParams.tournamentId};
                                 $q.all([Tournament.parameters(req).$promise,
                                         Tournament.aMine(req).$promise]).
                                     then(
                                         (responses) => {
                                             requestStatus.complete();
                                             $rootScope.$broadcast('event.tournament.rules.set',
                                                                   Object.assign({}, responses[1].toJSON(),
                                                                                 {rules: response[0].toJSON()}));
                                         },
                                         requestStatus.failed);
                             }
                         });
                     }]});
