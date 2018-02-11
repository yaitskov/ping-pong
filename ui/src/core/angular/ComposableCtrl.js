import AngularBean from './AngularBean.js';

export default class ComposableCtrl extends AngularBean {
    static get $inject() {
        return ['$rootScope'];
    }

    get isValid() {
        return true;
    }

    onInitChild() {
        console.log(`init composable controller ${this.constructor.name}`);
    }

    $onInit() {
        this.parent.registerChildCtrl(this);
        this.onInitChild();
        this.broadcast(this.constructor.readyEvent);
    }

    broadcast() {
        this.$rootScope.$broadcast(...arguments);
    }
}
