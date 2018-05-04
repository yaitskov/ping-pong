import AngularBean from 'core/angular/AngularBean.js';
import VoiceInputDialog from './VoiceInputDialog.js';

export default class PersonNameField extends AngularBean {
    constructor(...argument) {
        super(...arguments);
        this.restrict = 'E';
        this.require = ['^form', 'ngModel'];
        this.scope = {ngModel: '='};
        this.template = `<div class="input-group">
                             <input type="text"
                                    class="form-control" ng-model="ngModel"/>
                             <span class="input-group-btn">
                                <a class="btn btn-primary"
                                   title="click to active voice input"
                                   ng-click="showDialog()">
                                     <span class="glyphicon glyphicon-record"></span>
                                 </a>
                             </span>
                          </div>`;
    }

    static get $inject() {
        return ['MessageBus'];
    }

    nameSelected(name, ngModel) {
        ngModel.$setViewValue(name);
    }

    showDialog() {
        this.MessageBus.broadcast(VoiceInputDialog.TopicShow,
                                  'participant-name');
    }

    link(scope, element, attrs, ctrls) {
        this.MessageBus.subscribeIn(
            scope, VoiceInputDialog.TopicPick,
            (results) => this.nameSelected(name, ctrls[1]));
        scope.showDialog = () => this.showDialog();
    }
}
