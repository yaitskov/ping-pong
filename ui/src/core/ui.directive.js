import angular from 'angular';
import './directive/directive.import.js';

angular.module('core.ui').
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
    directive('autofocus', function () {
        return {
            restrict: 'A',
            link: function (scope, element) {
                element[0].focus();
            }
        };
    }).
    directive('simpleToggler', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                $(element[0]).click(function () {
                    var target = $(attrs.simpleToggler);
                    var visiable = target.hasClass('in');
                    target.collapse(visiable ? 'hide' : 'show');
                    if (visiable) {
                        $(this).addClass('collapsed');
                    } else {
                        $(this).removeClass('collapsed');
                    }
                });
            }
        };
    }).
    /**
       input bs-toggle
       ng-model="$ctrl.field"
       bs-toggle-on="Tak"
       bs-toggle-off="Nie"
       [ bs-toggle-target="#anchor" ]
       [ bs-toggle-options=="$ctrl.bsToggleOptions" ]
    */
    directive('bsToggle', ['$timeout', function ($timeout) {
        return {
            require: 'ngModel',
            restrict: 'A',
            scope: {
                bsToggleOptions: '&',
                ngChange: '&',
            },
            link: function (scope, element, attrs, ngModel) {
                function init(on, off) {
                    var options = Object.assign({on: on, off: off},
                                                scope.bsToggleOptions() || {});
                    $(element[0]).bootstrapToggle(options);
                    $(element[0]).change(function () {
                        var checked = $(this).prop('checked');
                        ngModel.$setViewValue(checked);
                    });
                    if (attrs.bsToggleDisabled) {
                        $(element[0]).bootstrapToggle('disable');
                    }
                    scope.$watch(
                        function () {
                            return ngModel.$modelValue;
                        },
                        function (checked) {
                            $(element[0]).prop('checked', checked).change();
                        });
                }
                init(attrs.bsToggleOn, attrs.bsToggleOff);
            }
        };
    }]).
    directive('defaultButtonType', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                $timeout(function () {
                    element.find('button').each(function (idx) {
                        if (!$(this).attr('type')) {
                            $(this).attr('type', 'button');
                        }
                    });
                }, 0);
            }
        };
    }]).
    directive('you', [function () {
        return {
            template: '<span ng-show="name == \'*you*\'">' +
                '{{name | translate}}' +
                '</span>' +
                '<span ng-show="name != \'*you*\'">' +
                '{{name}}' +
                '</span>',
            restrict: 'E',
            scope: {
                name: '='
            }
        };
    }]);
