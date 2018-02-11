import AngularBean from './AngularBean.js';

export default class ComposableCtrl extends AngularBean {
    static get $inject() {
        return ['$rootScope'];
    }

    get readyEvent() {
        throw new Error("implement me");
        // return `event.${this.constructor.name}.ready`;
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
        this.broadcast(this.readyEvent);
    }

    broadcast() {
        this.$rootScope.$broadcast(...arguments);
    }
}
