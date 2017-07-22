'use strict';

angular.module('myMatchPlayList').
    component('myMatchPlayList', {
        templateUrl: 'my-match-play-list/my-match-play-list.template.html',
        controller: ['Match', 'Tournament', 'mainMenu', '$q', 'cutil',
                     'pageCtx', 'auth', 'requestStatus', '$location',
                     function (Match, Tournament, mainMenu, $q, cutil,
                               pageCtx, auth, requestStatus, $location) {
                         mainMenu.setTitle('My matches to be played');
                         this.matches = null;
                         this.openMatch = null;
                         this.tournament = null;
                         this.previouslyScored = pageCtx.get('last-scoring-match') || {};
                         var self = this;
                         this.matchScoring = function () {
                             pageCtx.put('last-scoring-match', self.openMatch);
                             $location.path("/complete/my/match/" + self.openMatch.mid);
                         };
                         requestStatus.startLoading();
                         $q.all([Tournament.myRecent({}).$promise,
                                 Match.myMatchesNeedToPlay({}).$promise]).
                             then(
                                 function (responses) {
                                     self.tournament = responses[0];
                                     var matches = responses[1];
                                     requestStatus.complete();
                                     console.log("Loaded matches " + matches.length);
                                     self.matches = matches;
                                     self.openMatch = cutil.findValByO(matches, {state: 'Game'});
                                     if (self.openMatch) {
                                         pageCtx.putEnemyUid(auth.myUid(), self.openMatch.mid,
                                                             self.openMatch.enemy.uid);
                                     }
                                 },
                                 requestStatus.failed);
                     }
                    ]
        });
