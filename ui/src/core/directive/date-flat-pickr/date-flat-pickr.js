import angular from 'angular';
import './date-flat-pickr.scss';

angular.module('core.ui').
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
