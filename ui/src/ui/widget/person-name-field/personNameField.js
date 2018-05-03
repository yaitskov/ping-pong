export default function personNameField() {
    return {
        restrict: 'E',
        require: ['^form', 'ngModel'],
        scope: {
            ngModel: '=',
        },
        link: function (scope, element, attrs, ctrls) {
            scope.showDialog = () => {
                console.log("Show Voice Input Dialog");
            };
        },
        template: `<div class="input-group">
                     <input type="text"
                            class="form-control" ng-model="ngModel"/>
                     <span class="input-group-btn">
                       <a class="btn btn-primary"
                          title="click to active voice input"
                          ng-click="showDialog()">
                           <span class="glyphicon glyphicon-record"></span>
                       </a>
                     </span>
                   </div>`
    };
}
