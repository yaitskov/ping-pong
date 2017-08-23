import angular from 'angular';
import template from './main-menu.template.html';

angular.
    module('mainMenu').
    component('mainMenu', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$rootScope',
                     function (auth, mainMenu, $rootScope) {
                         var self = this;
                         this.accountName = auth.myName();
                         $rootScope.$on('title.set', function (event, title) {
                             self.title = title;
                         });
                         this.title = mainMenu.getTitle();
                         this.isAuthenticated = function () {
                             return auth.isAuthenticated();
                         };
                         this.isAdmin = function () {
                             return auth.userType() == 'Admin' || auth.userType() == 'Super';
                         };
                         this.contextMenu = function () {
                             return mainMenu.getContextMenu();
                         };
                     }]
    });
