import './info-popup.scss';
import SimpleController from 'core/angular/SimpleController.js';

export default class InfoPopupWidget extends SimpleController {
    static get $inject() {
        return ['syncTranslate'].concat(super.$inject);
    }

    static get TopicShow() {
        return 'info-popup-show';
    }

    static get TopicInvalidate() {
        return 'info-popup-invalidate';
    }

    $onInit() {
        this.subscribe(this.constructor.TopicShow,
                       (msg) => this.showMessage(msg));
        this.subscribe(this.constructor.TopicInvalidate,
                       (mId) => this.removeMessageById(mId));
        this.trans = this.syncTranslate.create();
        this.messages = [];
        this.freeId = 1;
    }

    showMessage(msg) {
        if (msg.format == 'translate') {
            this.trans.trans([msg.text, msg.params],
                             (translated) => {
                                msg.translated = translated;
                                this._showMessage(msg);
                             });
        } else {
            this._showMessage(msg);
        }
    }

    _showMessage(msg) {
        msg.level = msg.level || 'error';
        msg.id = this.freeId++;
        this.messages.unshift(msg);
        if (this.messages.length > 10) {
           this.messages.splice(10);
        }
    }

    removeMessageById(msgId) {
        const msgIdx = this.messages.findIndex((m) => m.id == msgId);
        if (msgIdx < 0) {
           return;
        }
        this.removeMessageByIndex(msgIdx);
    }

    removeMessageByIndex(index) {
        this.messages.splice(index, 1);
    }
}