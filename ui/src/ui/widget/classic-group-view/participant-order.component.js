import 'angular';
import template from './participant-order.template.html';

angular.
    module('widget').
    component('participantOrder', {
        templateUrl: template,
        controller: ['$scope', '$rootScope',
                     function ($scope, $rootScope) {
                         var self = this;
                         self.changeSortBy = (order) => {
                             $rootScope.$broadcast('event.classic.group.view.row.order', order);
                         };
                         $rootScope.$broadcast('event.participant.order.ready');
                     }]
    });
