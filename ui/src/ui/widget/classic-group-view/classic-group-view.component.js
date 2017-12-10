import 'angular';
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
                             self.tournamentId = tournament.tid;
                             self.participants.sort(sorters[self.rowOrder]);
                         };
                         self.setRowOrder = function (order) {
                             self.rowOrder = order;
                             self.participants.sort(sorters[self.rowOrder]);
                         };
                         self.setScoreShowMode = function (mode) {
                             self.scoreShowMode = mode;
                         };
                         binder($scope, {
                             'event.classic.group.view.data': (event, tournament) => self.loadData(tournament),
                             'event.classic.group.view.row.order': (event, order) => self.setRowOrder(order),
                             'event.classic.group.view.score.show.mode': (event, mode) => self.setScoreShowMode(mode)
                         });
                         $rootScope.$broadcast('event.classic.group.view.ready');
                     }]
    });
