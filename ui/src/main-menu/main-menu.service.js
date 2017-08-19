import angular from 'angular';

angular.
    module('mainMenu').
    factory('mainMenu', ['$rootScope', function ($rootScope) {
        return new function () {
            var self = this;
            this.title = '....';
            this.contextMenu = {};
            this.setTitle = function (title) {
                this.contextMenu = {};
                self.title = title;
            };
            this.getTitle = function () {
                return self.title;
            };
            this.setContextMenu = function (contextMenu) {
                this.contextMenu = contextMenu;
            };
            this.getContextMenu = function () {
                return this.contextMenu;
            };
        };
    }]);
