import angular from 'angular';

angular.
    module('mainMenu').
    factory('mainMenu', ['$rootScope', '$timeout', 'syncTranslate', function ($rootScope, $timeout, syncTranslate) {
        return new function () {
            var self = this;
            var stranslate = syncTranslate.create();
            var stranslateMenu = syncTranslate.create();
            this.title = '....';
            this.contextMenu = {};
            this.setTitle = function (originTitle) {
                self.contextMenu = {};
                stranslate.trans(originTitle || 'Loading', function (title) {
                    self.title = title;
                    $rootScope.$broadcast('title.set', title);
                });
            };
            this.getTitle = function () {
                return self.title;
            };
            this.setContextMenu = function (originMenu) {
                stranslateMenu.transMenu(originMenu, function (menu) {
                    self.contextMenu = menu;
                    $rootScope.$broadcast('menu.set', menu);
                });
            };
            this.getContextMenu = function () {
                return self.contextMenu;
            };
        };
    }]);
