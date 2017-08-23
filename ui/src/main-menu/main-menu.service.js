import angular from 'angular';

angular.
    module('mainMenu').
    factory('mainMenu', ['$rootScope', '$timeout', 'syncTranslate', function ($rootScope, $timeout, syncTranslate) {
        return new function () {
            var self = this;
            var stranslate = syncTranslate.create();
            this.title = '....';
            this.contextMenu = {};
            this.setTitle = function (originTitle) {
                this.contextMenu = {};
                stranslate.trans(originTitle, function (title) {
                    self.title = title;
                    $rootScope.$broadcast('title.set', title);
                });
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
