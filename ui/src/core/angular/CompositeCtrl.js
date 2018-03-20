import AngularBean from 'core/angular/AngularBean.js';
import childrenReadyBarier from './childrenReadyBarier.js';

export default class CompositeCtrl extends AngularBean {
    static get $inject() {
        return ['$rootScope', '$scope', 'binder', 'eBarier'];
    }

    get isValid() {
        for (let controller of this.childControllers) {
            if (!controller.isValid) {
                return false;
            }
        }
        return true;
    }

    constructor() {
        super(...arguments);
        this.childControllers = [];

        childrenReadyBarier(this);
    }

    $bind(eventHandlers) {
        this.binder(this.$scope, eventHandlers);
    }

    get expectedChildCtrls() {
        throw new Error('implement me');
    }

    registerChildCtrl(childCtrl) {
        console.log(`register ctrl ${childCtrl.constructor.name}`);
        this.childControllers.push(childCtrl);
    }

    broadcast() {
        this.$rootScope.$broadcast(...arguments);
    }
}
