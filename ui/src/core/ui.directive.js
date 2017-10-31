import angular from 'angular';

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
    directive('clockPicker', ['$timeout', '$q', function ($timeout, $q) {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                var ctx = {};
                ctx.e = element.clockpicker({twelvehour: true,
                                             vibrate: false,
                                             placement: 'bottom',
                                             align: 'right',
                                             afterShow: function () {
                                                 var val = $(ctx.e).data('clockpicker').input.val();
                                                 if (val && val.indexOf("PM") > 0) {
                                                     $('.pm-button').addClass('btn-primary');
                                                     $('.am-button').removeClass('btn-primary');
                                                 } else {
                                                     $('.am-button').addClass('btn-primary');
                                                     $('.pm-button').removeClass('btn-primary');
                                                 }
                                                 $('.am-button').click(function () {
                                                         $(this).addClass('btn-primary');
                                                         $('.pm-button').removeClass('btn-primary');
                                                     });
                                                 $('.pm-button').click(function (a, b, c) {
                                                     $(this).addClass('btn-primary');
                                                     $('.am-button').removeClass('btn-primary');
                                                 });
                                             }});
                $(ctx.e).data('clockpicker').amOrPm = ' AM';
            }
        };
    }]).
    /**  input ng-flatpickr
                 fp-opts="$ctrl.dateOpts"
                 fp-on-setup="$ctrl.dataPickerUi"
    */
    directive('ngFlatpickr', [function() {
        return {
            require: 'ngModel',
            restrict : 'A',
            scope : {
                fpOpts : '&',
                fpOnSetup : '&'
            },
            link : function(scope, element, attrs, ngModel) {
                var vp = new FlatpickrInstance(element[0], scope.fpOpts());
                if (scope.fpOnSetup) {
                    scope.fpOnSetup().fpItem = vp;
                }
                element.on('$destroy',function(){
                    vp.destroy();
                });
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
    }]).
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
