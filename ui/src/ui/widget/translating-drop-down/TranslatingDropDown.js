export default class TranslatingDropDown {
    constructor() {
        this.restrict = 'E';
        this.require = 'ngModel';
        this.scope = {
            domain:  '=',
            ngModel: '='
        };
        this.template = `<div class="btn-group">
                           <button type="button" class="btn navbar-btn btn-danger dropdown-toggle"
                                   data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                             {{ ngModel | translate }}
                             <span class="caret"></span>
                           </button>
                           <ul class="dropdown-menu">
                             <li ng-repeat="item in domain">
                               <a ng-click="selectItem(item)">
                                 {{ item | translate }}
                               </a>
                             </li>
                           </ul>
                           <input type="hidden" ng-model="ngModel"/>
                         </div>`;
    }

    link(scope, element, attrs, ngModel) {
        scope.selectItem = (item) => {
            ngModel.$setViewValue(item);
        };
    }
}
