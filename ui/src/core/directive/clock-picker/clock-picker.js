import './clock-picker.scss';
import angular from 'angular';

angular.module('core.ui').
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
    }]);
