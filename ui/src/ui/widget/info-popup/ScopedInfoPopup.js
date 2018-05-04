import InfoPopupWidget from 'ui/widget/info-popup/InfoPopupWidget.js';

export default class ScopedInfoPopup {
    constructor(forward) {
        this.forward = forward;
        this.messages = [];
    }

    clearAll() {
        const bus = this.forward.MessageBus;
        this.messages.
            filter(m => m.id).
            map(m => m.id).
            forEach(mid => bus.broadcast(InfoPopupWidget.TopicInvalidate, mid));
        this.messages = [];
    }

    transError(...args) {
        this.messages.push(this.forward.transError(...args));
    }

    transInfo(...args) {
        this.messages.push(this.forward.transInfo(...args));
    }

    transWarn(...args) {
        this.messages.push(this.forward.transWarn(...args));
    }

    transSuccess(...args) {
        this.messages.push(this.forward.transSuccess(...args));
    }
}