export default function manyButtonToggler() {
    return {
        restrict: 'E',
        require: 'ngModel',
        scope: {
            id: '@',
            domain: '=',
            label: '@',
            selectedClass: '=',
            onClick: '=',
            onClick1: '='
        },
        link: function (scope, element, attrs, ngModel) {
            scope.up = (val) => {
                if (scope.onClick) {
                    const idx = scope.domain.indexOf(val);
                    if (scope.onClick.length) {
                        if (scope.onClick[idx]) {
                            scope.onClick[idx](idx);
                        }
                    } else {
                        scope.onClick(idx);
                    }
                }
                ngModel.$setViewValue(val);
                ngModel.$render();
            };
            scope.pickSelectedClass = (index) => {
                if (!scope.selectedClass) {
                    return 'btn-primary';
                }
                if (typeof scope.selectedClass === 'string') {
                    return scope.selectedClass;
                }
                return scope.selectedClass[index];
            };
            scope.getValue123 = () => {
                return ngModel.$modelValue;
            };
        },
        template: `<div class="form-group">
                         <label class="toggle-option">{{ label | translate }}</label>
                         <div class="btn-toolbar">
                            <a class="btn btn-default"
                               ng-click="up(domainValue)"
                               ng-repeat="domainValue in domain"
                               ng-class="{'{{pickSelectedClass($index)}}': getValue123() == domainValue}">{{
                                 (label + '-' + domainValue) | translate }}</a>
                         </div>
                   </div>`
    };
}
