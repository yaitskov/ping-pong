import InfoPopupWidget from 'ui/widget/info-popup/InfoPopupWidget.js';

export default class ScopedInfoPopup {
    constructor(forward) {
        this.forward = forward;
        this.messages = [];
    }

    clearAll() {
        this.messages.
            filter(m => m.id).
            map(m => m.id).
            forEach(mid => this._sendRemove(mid));
        this.messages = [];
    }

    _sendRemove(mid) {
        this.forward.MessageBus.broadcast(InfoPopupWidget.TopicInvalidate, mid);
    }

    transError(...args) {
        const msg = this.forward.transError(...args);
        this.messages.push(msg);
        return msg;
    }

    transInfo(...args) {
        const msg = this.forward.transInfo(...args);
        this.messages.push(msg);
        return msg;
    }

    transWarn(...args) {
        const msg = this.forward.transWarn(...args);
        this.messages.push(msg);
        return msg;
    }

    transSuccess(...args) {
        const msg = this.forward.transSuccess(...args);
        this.messages.push(msg);
        return msg;
    }

    removeMessage(msg) {
        const idx = this.messages.findIndex(msg.id
            ? (m) => m.id == msg.id
            : (m) => m.text == msg.text);
        if (idx < 0) {
            console.error(`not found message ${msg.id}/${msg.text}`);
            return;
        }
        this._sendRemove(msg.id);
        this.messages.splice(idx, idx + 1);
    }
}