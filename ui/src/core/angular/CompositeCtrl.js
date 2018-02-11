import AngularBean from 'core/angular/AngularBean.js';

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

        const eventNames = this.expectedChildCtrls.map(cls => cls.readyEvent);
        const readyChildCtrls = this.eBarier.create(
            eventNames, () => this.broadcast(this.constructor.readyEvent));
        const eventHandlers = {};
        for (let event of eventNames) {
            eventHandlers[event] = (e) => readyChildCtrls.got(event);
        }
        this.$bind(eventHandlers);
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
