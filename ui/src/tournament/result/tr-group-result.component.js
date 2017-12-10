import angular from 'angular';
import template from './tr-group-result.template.html';

angular.
    module('tournament').
    component('tournamentGroupResult', {
        templateUrl: template,
        controller: ['Tournament', 'Group', 'mainMenu', '$routeParams', 'eBarier',
                     'requestStatus', 'binder', '$scope', '$rootScope',
                     function (Tournament, Group, mainMenu, $routeParams, eBarier,
                               requestStatus, binder, $scope, $rootScope) {
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.activeGroup = null;
                         self.currentCid = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};

                         self.loadGroups = function () {
                                 requestStatus.startLoading();
                                 Group.list(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (tournament) {
                                         requestStatus.complete();
                                         self.allGroups = tournament.groups;
                                         self.categories = tournament.categories;

                                         $rootScope.$broadcast('event.category.switch.data', {list: self.categories});
                                     },
                                     requestStatus.failed);
                         };

                         var barWidgetsReady = eBarier.create(['view', 'group', 'category', 'status'], (e) => {
                             self.loadGroups();
                         });

                         self.pickCategory = function (cid) {
                             self.activeGroup = [];
                             for (var gi in self.allGroups) {
                                 var group = self.allGroups[gi];
                                 if (group.cid == cid) {
                                    self.activeGroup.push(group);
                                 }
                             }
                             $rootScope.$broadcast('event.group.switch.data', {list: self.activeGroup});
                         };
                         self.pickGroup = function (gid) {
                             requestStatus.startLoading();
                             Group.result(
                                     {tournamentId: $routeParams.tournamentId, groupId: gid},
                                     function (tournament) {
                                         requestStatus.complete();
                                         $rootScope.$broadcast('event.classic.group.view.data', tournament);
                                     },
                                     requestStatus.failed);
                         };
                         binder($scope, {
                             'event.main.menu.ready': function (e) { mainMenu.setTitle('Results in groups'); },
                             'event.request.status.ready': function (event) { barWidgetsReady.got('status'); },
                             'event.classic.group.view.ready': function (e) { barWidgetsReady.got('view'); },
                             'event.category.switch.ready': function (e) { barWidgetsReady.got('category'); },
                             'event.group.switch.ready': function (e) { barWidgetsReady.got('group'); },
                             'event.group.switch.current': function (e, gid) { self.pickGroup(gid); },
                             'event.category.switch.current': function (e, cid) { self.pickCategory(cid); }
                         });
                     }
                    ]
        });
