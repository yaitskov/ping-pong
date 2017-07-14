'use strict';

angular.
    module('mainMenu').
    component('mainMenu', {
        templateUrl: 'main-menu/main-menu.template.html',
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
                             return auth.isAdmin();
                         };
                         this.logout = function () {
                             auth.logout();
                         };
                         this.contextMenu = function () {
                             return mainMenu.getContextMenu();
                         };
                     }]
    });
