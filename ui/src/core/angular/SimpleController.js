import AngularBean from './AngularBean.js';

export default class SimpleController extends AngularBean {
    static get $inject() {
        return ['$scope', 'MessageBus'];
    }

    subscribe(topicName, callback) {
        return this.MessageBus.subscribeIn(this.$scope, topicName, callback);
    }

    send(topicName, ...eventParams) {
        this.MessageBus.broadcast(topicName, ...eventParams);
    }
}
