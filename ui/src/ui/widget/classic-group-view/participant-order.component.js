import 'angular';
import template from './participant-order.template.html';

angular.
    module('widget').
    component('participantOrder', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder',
                     function ($scope, $rootScope, binder) {
                         var self = this;
                         self.sortBy = 'seed';
                         self.changeSortBy = (order) => {
                             $rootScope.$broadcast('event.classic.group.view.row.order', order);
                         };
                         binder($scope, {
                             'event.classic.group.view.row.order': function (e, order) {
                                  self.sortBy = order;
                             }
                         })
                         $rootScope.$broadcast('event.participant.order.ready');
                     }]
    });
