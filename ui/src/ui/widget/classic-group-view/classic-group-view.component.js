import 'angular';
import './classic-group-view.scss';
import template from './classic-group-view.template.html';

angular.
    module('widget').
    component('classicGroupView', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder', '$attrs',
                     function ($scope, $rootScope, binder, $attrs) {
                         var self = this;

                         var sorters = {
                             'seed':  (a, b) => a.seedPosition - b.seedPosition,
                             'final': (a, b) => a.finishPosition - b.finishPosition,
                             'name':   (a, b) => a.name.localeCompare(b.name)
                         };
                         self.scoreShowMode = 'sets';
                         self.rowOrder = 'seed'; // final | abc
                         self.loadData = function (tournament) {
                             self.participants = tournament.participants;
                             for (var pi in self.participants) {
                                 var p = self.participants[pi];
                                 if (p.setsAndBalls) {
                                    self.showBalance = true;
                                 }
                             }
                             self.tournamentId = tournament.tid;
                             self.quitsGroup = tournament.quitsGroup;
                             self.disambiguationPolicy = tournament.disambiguationPolicy;
                             self.participants.sort(sorters[self.rowOrder]);
                         };
                         self.setRowOrder = function (order) {
                             self.rowOrder = order;
                             self.participants.sort(sorters[self.rowOrder]);
                         };
                         self.setScoreShowMode = function (mode) {
                             self.scoreShowMode = mode;
                         };
                         self.isLost = function (p1, p2) {
                             var m = p1.matches[p2.uid];
                             if (m) {
                                return m.sets.his < m.sets.enemy;
                             }
                         };
                         self.isWon = function (p1, p2) {
                             var m = p1.matches[p2.uid];
                             if (m) {
                                return m.sets.his > m.sets.enemy;
                             }
                         };
                         binder($scope, {
                             'event.classic.group.view.data': (event, tournament) => self.loadData(tournament),
                             'event.classic.group.view.row.order': (event, order) => self.setRowOrder(order),
                             'event.classic.group.view.score.show.mode': (event, mode) => self.setScoreShowMode(mode)
                         });
                         $rootScope.$broadcast('event.classic.group.view.ready');
                     }]
    });
