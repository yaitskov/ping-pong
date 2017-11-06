import angular from 'angular';
import template from './review-match-score-admin.template.html';

angular.
    module('tournament').
    component('reviewMatchScoreForAdmin', {
        templateUrl: template,
        controller: ['mainMenu', 'pageCtx', '$routeParams', 'binder', '$rootScope', '$scope',
                     function (mainMenu, pageCtx, $routeParams, binder, $rootScope, $scope) {
                         this.tournamentId = $routeParams.tournamentId;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match Review'),
                             'event.review.match.ready': (event) => {
                                 $rootScope.$broadcast(
                                     'event.review.match.data',
                                     pageCtx.get('match-score-review-' + $routeParams.matchId));
                             }
                         });
                     }]});
