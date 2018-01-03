import angular from 'angular';
import '../../tournament-result.scss';
import template from './participant-group.template.html';

angular.
    module('participant').
    component('participantGroup', {
        templateUrl: template,
        controller: ['Group', 'mainMenu', '$routeParams', 'requestStatus', '$rootScope', '$scope', 'binder',
                     function (Group, mainMenu, $routeParams, requestStatus, $rootScope, $scope, binder) {
                         var self = this;
                         self.groupId = $routeParams.groupId;
                         self.tournamentId = $routeParams.tournamentId;
                         requestStatus.startLoading();
                         Group.infoWithMembers({tournamentId: self.tournamentId, groupId: self.groupId},
                                       function (groupInfo) {
                                           requestStatus.complete();
                                           self.group = groupInfo;
                                           self.members = groupInfo.members;
                                       },
                                       requestStatus.failed);
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Group members'),
                         });
                     }
                    ]
        });
