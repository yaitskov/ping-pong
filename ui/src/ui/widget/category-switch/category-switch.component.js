import angular from 'angular';
import template from './category-switch.template.html';

angular.
    module('widget').
    component('categorySwitch', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder',
                     function ($scope, $rootScope, binder) {
                         var self = this;
                         self.pickCategory = function (cid) {
                             $rootScope.$broadcast('event.category.switch.current', cid);
                         };
                         binder($scope, {
                             'event.category.switch.data': (event, categories) => {
                                 self.categories = categories.list;
                                 self.currentCid = categories.currentCid || (categories.list.length ? categories.list[0].cid : 0);
                                 if (self.currentCid) {
                                     $rootScope.$broadcast('event.category.switch.current', self.currentCid);
                                 }
                             },
                             'event.category.switch.current': (event, cid) => {
                                 self.currentCid = cid;
                             }
                         });
                         $rootScope.$broadcast('event.category.switch.ready');
                     }]});
