import angular from 'angular';
import './vis-ladder.css';
import template from './vis-ladder.template.html';
import VisLadder from './VisLadder.js';

angular.
    module('widget').
    component('visLadder', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder', '$attrs', '$window', 'cutil',
                     function ($scope, $rootScope, binder, $attrs, $window, cutil) {
                         let container = document.getElementById('play-off-chart');
                         binder($scope, {
                             'event.playoff.view.data': (e, tournament) => new VisLadder(container, tournament),
                         });
                         $rootScope.$broadcast('event.playoff.view.ready');
                     }]
    });
