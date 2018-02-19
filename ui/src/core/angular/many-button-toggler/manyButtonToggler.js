export default function manyButtonToggler() {
    return {
        restrict: 'E',
        require: 'ngModel',
        scope: {
            id: '@',
            domain: '=',
            label: '@',
            selectedClass: '@'
        },
        link: function (scope, element, attrs, ngModel) {
            scope.selectedClasss = scope.selectedClasss || 'btn-primary';
            scope.up = (val) => {
                ngModel.$setViewValue(val);
                ngModel.$render();
            };
            scope.getValue123 = () => {
                return ngModel.$modelValue;
            };
        },
        template: `<div class="form-group">
                         <label class="toggle-option">{{ label | translate }}</label>
                         <a class="btn btn-default"
                            ng-click="up(domainValue)"
                            ng-repeat="domainValue in domain"
                            ng-class="{'{{selectedClass}}': getValue123() == domainValue}">{{
                              (label + '-' + domainValue) | translate }}</a>
                   </div>`
    };
}
