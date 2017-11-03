import angular from 'angular';
import template from './review-match-score-user.template.html';

angular.
    module('tournament').
    component('reviewMatchScoreForUser', {
        templateUrl: template,
        controller: ['mainMenu', '$routeParams', 'binder', '$rootScope', '$scope',
                     function (mainMenu, $routeParams, binder, $rootScope, $scope) {
                         mainMenu.setTitle('Match Review');
                         this.tournamentId = $routeParams.tournamentId;
                         binder($scope, {
                             'event.review.match.ready': (event) => {
                                 $rootScope.$broadcast(
                                     'event.review.match.data',
                                     pageCtx.get('match-score-review-' + $routeParams.matchId));
                             }
                         });
                     }]});
