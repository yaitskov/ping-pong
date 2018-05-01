import 'angular';
import template from './participant-order.template.html';
import ClassicGroupViewCtrl from 'ui/widget/classic-group-view/ClassicGroupViewCtrl.js';

angular.
    module('widget').
    component('participantOrder', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder', 'MessageBus',
                     function ($scope, $rootScope, binder, MessageBus) {
                         var self = this;
                         self.changeSortBy = (order) => MessageBus.broadcast(
                             ClassicGroupViewCtrl.TopicSetRowOrder, order);
                         MessageBus.subscribeIn(
                             $scope,
                             ClassicGroupViewCtrl.TopicSetRowOrder,
                             (order) => self.sortBy = order);
                         self.changeSortBy('final');
                         $rootScope.$broadcast('event.participant.order.ready');
                     }]
    });
