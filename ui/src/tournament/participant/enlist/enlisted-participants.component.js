import angular from 'angular';
import template from './enlisted-participants.template.html';

angular.
    module('participant').
    component('enlistedParticipants', {
        templateUrl: template,
        controller: ['Category', 'pageCtx', 'mainMenu', '$routeParams', 'requestStatus', '$rootScope', '$scope', 'binder',
                     function (Category, pageCtx, mainMenu, $routeParams, requestStatus, $rootScope, $scope, binder) {
                         var self = this;
                         self.categories = pageCtx.get('categories');
                         self.tournamentId = $routeParams.tournamentId;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Enlisted to tournament'),
                             'event.category.switch.current': (event, cid) => {
                                 requestStatus.startLoading("Loading participants");
                                 Category.members({categoryId: cid},
                                                  function (members) {
                                                      requestStatus.complete();
                                                      self.members = members;
                                                  },
                                                  requestStatus.failed);
                             },
                             'event.category.switch.ready': (event) => {
                                 $rootScope.$broadcast('event.category.switch.data', self.categories);
                             }
                         });
                     }
                    ]
        });
