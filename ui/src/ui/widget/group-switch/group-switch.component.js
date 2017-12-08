import angular from 'angular';
import template from './group-switch.template.html';

angular.
    module('widget').
    component('groupSwitch', {
        templateUrl: template,
        controller: ['$scope', '$rootScope', 'binder',
                     function ($scope, $rootScope, binder) {
                         var self = this;
                         self.pickGroup = function (gid) {
                             $rootScope.$broadcast('event.group.switch.current', gid);
                         };
                         binder($scope, {
                             'event.group.switch.data': (event, groups) => {
                                 self.groups = groups.list;
                                 self.currentGid = groups.currentGid || (groups.list.length ? groups.list[0].gid : 0);
                                 if (self.currentGid) {
                                     $rootScope.$broadcast('event.group.switch.current', self.currentGid);
                                 }
                             },
                             'event.group.switch.current': (event, gid) => {
                                 self.currentGid = gid;
                             }
                         });
                         $rootScope.$broadcast('event.group.switch.ready');
                     }]});
