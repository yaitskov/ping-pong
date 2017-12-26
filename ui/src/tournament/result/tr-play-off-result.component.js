import 'angular';
import template from './tr-play-off-result.template.html';

angular.
    module('tournament').
    component('tournamentPlayOffResult', {
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

                         self.loadCategories = function () {
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

                         var barWidgetsReady = eBarier.create(['view', 'category', 'status'], (e) => {
                             self.loadCategories();
                         });

                         self.pickCategory = function (cid) {
                             Tournament.playOffMatches(
                                {tournamentId: $routeParams.tournamentId,
                                 categoryId: cid},
                                (tournament) => {
                                    tournament.tid = $routeParams.tournamentId;
                                    $rootScope.$broadcast('event.playoff.view.data', tournament);
                                },
                                requestStatus.failed);

                         };

                         binder($scope, {
                             'event.main.menu.ready': function (e) { mainMenu.setTitle('PlayOff ladder'); },
                             'event.request.status.ready': function (event) { barWidgetsReady.got('status'); },
                             'event.playoff.view.ready': function (e) { barWidgetsReady.got('view'); },
                             'event.category.switch.ready': function (e) { barWidgetsReady.got('category'); },
                             'event.category.switch.current': function (e, cid) { self.pickCategory(cid); }
                         });
                     }
                    ]
        });
