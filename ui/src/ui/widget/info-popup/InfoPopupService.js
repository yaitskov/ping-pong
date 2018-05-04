import AngularBean from 'core/angular/AngularBean.js';
import InfoPopupWidget from './InfoPopupWidget.js';

export default class InfoPopupService extends AngularBean {
    static get $inject() {
        return ['MessageBus'];
    }

    showMessage(msg) {
        this.MessageBus.broadcast(InfoPopupWidget.TopicShow, msg);
    }

    transError(msg, params = {}) {
        this.showMessage({format: 'translate', level: 'danger', text: msg, params: params});
        return msg;
    }

    transInfo(msg, params = {}) {
        this.showMessage({format: 'translate', level: 'info', text: msg, params: params});
        return msg;
    }

    transWarn(msg, params = {}) {
        this.showMessage({format: 'translate', level: 'warning', text: msg, params: params});
        return msg;
    }

    transSuccess(msg, params = {}) {
        this.showMessage({format: 'translate', level: 'success', text: msg, params: params});
        return msg;
    }
}
