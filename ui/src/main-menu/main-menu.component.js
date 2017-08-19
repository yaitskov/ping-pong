import angular from 'angular';
import template from './main-menu.template.html';

angular.
    module('mainMenu').
    component('mainMenu', {
        templateUrl: template,
        controller: ['auth', 'mainMenu',
                     function (auth, mainMenu) {
                         this.accountName = auth.myName();
                         this.title = function () {
                             return mainMenu.getTitle();
                         };
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
