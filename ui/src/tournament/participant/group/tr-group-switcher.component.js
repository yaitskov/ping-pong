import angular from 'angular';
import template from './tr-group-switcher.template.html';

//class TrGroupSwitcher {
//
//    $onInit() {
//    }
//
//    constructor() {
//    }
//}

angular.module('participant').
    component('trGroupMemberSwitcher', {
        templateUrl: template,
        controller: ['$q', '$http', 'mainMenu', '$routeParams', 'auth', 'binder', '$scope',
                     'requestStatus', 'pageCtx', '$location', 'Group', 'Participant',
                     function ($q, $http, mainMenu, $routeParams, auth, binder, $scope,
                               requestStatus, pageCtx, $location, Group, Participant) {
                         var self = this;
                         self.groups = null;
                         self.bid = null;
                         self.tournamentId = $routeParams.tournamentId;
                         self.participantId = $routeParams.participantId;

                         self.assignGroup = function (targetGid) {
                             if (targetGid == self.bid.group.gid) {
                                 history.back();
                                 return;
                             }
                             requestStatus.startLoading('Changing group');
                             let req = {expectedGid: self.bid.group.gid,
                                        tid: $routeParams.tournamentId,
                                        uid: $routeParams.participantId};
                             if (targetGid !== 0) {
                                 req.targetGid = targetGid;
                             }
                             Participant.changeGroup(req, () => history.back(), requestStatus.failed);
                         };

                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 var ctxMenu = {};
                                 ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                                 mainMenu.setTitle('Change group', ctxMenu);
                             },
                             'event.request.status.ready': (e) => {
                                 requestStatus.startLoading('Loading groups');
                                 $q.all([
                                     Group.list({tournamentId: $routeParams.tournamentId}).$promise,
                                     Participant.profile({
                                         tournamentId: $routeParams.tournamentId,
                                         participantId: $routeParams.participantId
                                     }).$promise
                                 ]).then(
                                     (responses) => {
                                         requestStatus.complete();
                                         self.groups = responses[0].groups;
                                         self.bid = responses[1];
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
