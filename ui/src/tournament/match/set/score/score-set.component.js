import angular from 'angular';
import template from './score-set.template.html';

angular.
    module('scoreSet').
    component('scoreSet', {
        templateUrl: template,
        controller: ['Match', 'requestStatus', '$scope', '$rootScope', 'binder',
                     function (Match, requestStatus, $scope, $rootScope, binder) {
                         var self = this;
                         binder($scope, {
                             'event.base.match.set.ready': (event) => {
                                 $rootScope.$broadcast('event.match.set.ready');
                             },
                             'event.match.set.scored': (event, matchScore) => {
                                 Match.scoreMatch(
                                     matchScore,
                                     function (okResp) {
                                         requestStatus.complete();
                                         if (okResp.scoreOutcome == 'MatchComplete' || okResp.scoreOutcome == 'LastMatchComplete') {
                                             $rootScope.$broadcast('event.match.scored', okResp.matchScore);
                                         } else if (okResp.scoreOutcome == 'MatchContinues') {
                                             $rootScope.$broadcast('event.match.set.next', okResp);
                                         } else {
                                             requestStatus.validationFailed(["Match score response unknown", {name: okResp.scoreOutcome}]);
                                         }
                                     },
                                     function (resp) {
                                         if (resp.status == 400) {
                                             if (resp.data.error == 'matchScored') {
                                                 requestStatus.complete();
                                                 $rootScope.$broadcast('event.match.score.raise.conflict', resp);
                                             } else {
                                                 requestStatus.failed(resp);
                                             }
                                         } else {
                                             requestStatus.failed(resp);
                                         }
                                     });
                             }
                         });
                         self.scoreMatchSet = function () {
                             $rootScope.$broadcast('event.match.set.score');
                         };
                     }]});
