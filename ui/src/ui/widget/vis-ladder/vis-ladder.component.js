import angular from 'angular';
import vis from 'vis';

import template from './vis-ladder.template.html';

angular.
    module('widget').
    component('visLadder', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder', '$attrs', '$window', 'cutil',
                     function ($scope, $rootScope, binder, $attrs, $window, cutil) {
                         var self = this;

                         self.matchLabel = function (tournament, match) {
                             var uids = Object.keys(match.score);
                             if (match.state == 'Over') {
                                 var result = [];
                                 if (match.walkOver) {
                                     for (var i = 0; i < 2; ++i) {
                                         var name = (uids[i] == 1) ? 'bye' : tournament.participants[uids[i]];
                                         result.push((uids[i] == match.winnerId ? ' ☺' : " ⚐") +  " - " + name);
                                     }
                                 } else {
                                     for (var i = 0; i < 2; ++i) {
                                         var name = (uids[i] == 1) ? 'bye' : tournament.participants[uids[i]];
                                         result.push(cutil.padLeft("" + match.score[uids[i]], ' ', 2)
                                                     + (uids[i] == match.winnerId ? ' + ' : " - ") + name);
                                     }
                                 }
                                 return result.join("\n");
                             } else if (match.state == 'Draft') {
                                 if (uids.length == 1) {
                                     return " ⌛ - " + tournament.participants[uids[0]] + "\n ⌛ - ???";
                                 } else {
                                     return " ??? ";
                                 }
                             } else if (match.state == 'Place') {
                                 var result = [];
                                 for (var i = 0; i < 2; ++i) {
                                     var name = tournament.participants[uids[i]];
                                     result.push(" ⌛ - " + name);
                                 }
                                 return result.join("\n");
                             } else if (match.state == 'Game') {
                                 var result = [];
                                 for (var i = 0; i < 2; ++i) {
                                     var name = tournament.participants[uids[i]];
                                     result.push(cutil.padLeft("" + match.score[uids[i]], ' ', 2) + " - " + name);
                                 }
                                 return result.join("\n");
                             } else if (match.state == 'Auto') {
                                 return " ⚐ - " + tournament.participants[uids[0]] + "\n ☺ - ???";
                             } else {
                                 return 'state: ' + match.state;
                             }
                         };

                         self.createMatchNode = function (tournament, match) {
                             var node = {id: match.id, level: match.level,
                                         label: self.matchLabel(tournament, match),
                                         shape: 'box'};
                             return node;
                         };

                         self.loadData = function (tournament) {
                             var nodes = [];
                             for (var i in tournament.matches) {
                                 var match = tournament.matches[i];
                                 nodes.push(self.createMatchNode(tournament, match));
                             }
                             var nodeSet = new vis.DataSet(nodes);

                             var edges = [];
                             for (var i in tournament.transitions) {
                                 var edge = tournament.transitions[i];
                                 edge.arrow = 'to';
                                 edges.push(edge);
                             }

                             var edgeSet = new vis.DataSet(edges);
                             var container = document.getElementById('play-off-chart');
                             var data = {
                                 nodes: nodeSet,
                                 edges: edgeSet
                             };
                             var options = {
                                 autoResize: true,
                                 height: '100%',
                                 width: '100%',
                                 manipulation: false,
                                 nodes: {
                                     font: {
                                         face: 'mono'
                                     }
                                 },
                                 edges: {
                                     arrows: 'to'
                                 },
                                 layout: {
                                     hierarchical: {
                                         direction: 'LR',
                                         enabled: true,
                                         levelSeparation: 400,
                                         sortMethod: "directed"
                                     },
                                     //randomSeed: 2
                                 },
                                 physics: {
                                     hierarchicalRepulsion: {
                                         nodeDistance: 80,
                                     }
                                 }
                             };

                             self.network = new vis.Network(container, data, options);
                         };
                         binder($scope, {
                             'event.playoff.view.data': (event, tournament) => self.loadData(tournament),
                         });
                         $rootScope.$broadcast('event.playoff.view.ready');
                     }]
    });