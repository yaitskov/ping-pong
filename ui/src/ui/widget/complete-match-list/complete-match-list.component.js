import 'angular';
import template from './complete-match-list.template.html';

angular.
    module('widget').
    component('completeMatchList', {
        templateUrl: template,
        bindings: {
            tournamentId: '='
        },
        controller: ['$scope', '$rootScope', 'binder', '$attrs',
                     function ($scope, $rootScope, binder, $attrs) {
                         var self = this;
                         var map = {};
                         map['event.complete.match.list.data.' + $attrs.label] = (e, list) => {
                             self.matchList = list;
                         };
                         binder($scope, map);
                         $rootScope.$broadcast('event.complete.match.list.ready.' + $attrs.label);
                     }]
    });
