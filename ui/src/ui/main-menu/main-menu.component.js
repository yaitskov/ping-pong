import angular from 'angular';
import './main-menu.scss';
import template from './main-menu.template.html';

angular.
    module('mainMenu').
    component('mainMenu', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$rootScope', 'binder', '$scope', '$window', 'pageCtx',
                     function (auth, mainMenu, $rootScope, binder, $scope, $window, pageCtx) {
                         var self = this;
                         self.accountName = auth.myName();
                         self.title = mainMenu.getTitle();
                         self.lastTournament = pageCtx.get('last-tournament');
                         self.isAuthenticated = function () {
                             return auth.isAuthenticated();
                         };
                         self.isAdmin = function () {
                             return auth.userType() == 'Admin' || auth.userType() == 'Super';
                         };
                         self.contextMenu = mainMenu.getContextMenu();
                         binder($scope, {
                             'event.mm.last.tournament': (event, tournament) => {
                                 pageCtx.put('last-tournament', tournament);
                                 self.lastTournament = tournament;
                             },
                             'menu.set': (event, menu) => {
                                 self.contextMenu = menu;
                             },
                             'title.set': (event, title) => {
                                 $window.document.title = title;
                                 self.title = title;
                             }
                         });
                         $rootScope.$broadcast('event.main.menu.ready');
                     }]
    });
