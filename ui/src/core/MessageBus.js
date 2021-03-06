import AngularBean from './angular/AngularBean.js';

class Topic {
    constructor(bus, name) {
        this.bus = bus;
        this.name = name;
        this.subscribers = [];
        this.lastEvent = undefined;
    }

    subscribe(consumer, scope) {
        this.subscribers.push({consumer: consumer,
                               scope: scope});
        if (this.lastEvent != undefined) {
            consumer(...this.lastEvent);
        }
        return () => this.unsubscribe(consumer);
    }

    unsubscribe(consumer) {
        const idx = this.subscribers.findIndex(e => e.consumer == consumer);
        if (idx < 0) {
            console.warn(`Attempt to unsubscribe unknown consumer ${consumer} from topic ${this.name}`);
            return false;
        }
        this.subscribers.splice(idx, 1);
        if (!this.subscribers.length) {
            this.bus._removeTopic(this.name);
        }
        return true;
    }

    broadcast(params) {
        this.subscribers.forEach(s => {
            s.consumer(...params);
            if (s.scope) {
                this.bus.$timeout(() => s.scope.$digest());
            }
        });
        this.lastEvent = params;
    }
}

export default class MessageBus extends AngularBean {
    static get $inject() {
        return ['$timeout'];
    }

    constructor(...args) {
        super(...args);
        this.topicsByName = {};
    }

    _newTopic(name) {
        return this.topicsByName[name] = new Topic(this, name);
    }

    subscribe(topicName, consumer, scope) {
        let topic = this.topicsByName[topicName];
        if (!topic) {
            topic = this._newTopic(topicName);
        }
        return topic.subscribe(consumer, scope);
    }

    subscribeIn($scope, topicName, consumer) {
        $scope.$on('$destroy', this.subscribe(topicName, consumer, $scope));
    }

    _removeTopic(topicName) {
        delete this.topicsByName[topicName];
    }

    broadcast(topicName, ...params) {
        let topic = this.topicsByName[topicName];
        if (!topic) {
            console.info(`Broadcast to topic [${topicName}] without consumers`);
            topic = this._newTopic(topicName);
        }
        topic.broadcast(params);
    }
}
