import AngularBean from 'core/angular/AngularBean.js';

export default class ConsoleTournamentRuleLink extends AngularBean {
    constructor(...args) {
        super(...args);
        this.restrict = 'E';
        this.require = 'ngModel';
        this.scope = {
            parentTid: '=',
            consoleTid: '=',
            consoleType: '@', // ConGru | ConOff
            ngModel: '='
        };
        this.template = `
<many-button-toggler domain="['ir', 'NO']" selected-class="['btn-primary', 'btn-danger']"
                     label="Play-console-tournament"
                     ng-model="ngModel"/>
<div class="form-group" ng-show="ngModel != 'NO' && consoleTid">
    <a class="btn btn-primary"
       href="#!/my/tournament/parameters/{{consoleTid}}"
       translate="configure-console-tournament"/>
</div>`;
    }

    static get $inject() {
        return ['AjaxInfo'];
    }

    link(scope, element, attrs, ngModel) {
        this.ajax = this.AjaxInfo.scope(scope);

        scope.$watch(
            () => ngModel.$modelValue,
            (newV) => {
                if (newV == 'ir') {
                    if (!scope.consoleTid) {
                        this.createConsoleTournament(scope);
                    }
                }
            } );
    }

    createConsoleTournament(scope) {
        this.ajax.doPost(
            'creating-console-tournament',
            '/api/tournament/console/create',
            {parentTid: scope.parentTid,
             consoleType: scope.consoleType},
            (consoleTid) => scope.consoleTid = consoleTid);
    }
}
