'use strict';

angular.module('core.ui', ['ngResource']).
    directive('backButton', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                element.bind('click', goBack);

                function goBack() {
                    history.back();
                }
            }
        };
    }).
    directive('clickable', ['$location', function ($location) {
        return {
            scope: {},
            restrict: 'A',
            link: function (scope, elem, attrs) {
                var moved = false;
                elem.on('mouseover', function () {
                    elem.addClass('hover');
                });
                elem.on('mouseout', function () {
                    elem.removeClass('hover');
                });
                elem.on('mousedown', function () {
                    moved = false;
                    elem.addClass('clicked');
                });
                elem.on('mousemove', function () {
                    moved = true;
                });
                elem.on('mouseup', function () {
                    elem.removeClass('clicked');
                    if (moved) {
                        return;
                    }
                    var url = attrs.clickable;
                    if (url) {
                        scope.$apply(function () {
                            $location.path(url);
                        });
                    }
                });
            }
        };
    }]);
