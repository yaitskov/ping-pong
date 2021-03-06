import AngularBean from 'core/angular/AngularBean.js';
import InfoPopupWidget from './InfoPopupWidget.js';
import ScopedInfoPopup from './ScopedInfoPopup.js';

export default class InfoPopupService extends AngularBean {
    static get $inject() {
        return ['MessageBus'];
    }

    showMessage(msg) {
        this.MessageBus.broadcast(InfoPopupWidget.TopicShow, msg);
        return msg;
    }

    transError(msg, params = {}) {
        return this.showMessage({format: 'translate', level: 'danger', text: msg, params: params});
    }

    transInfo(msg, params = {}) {
        return this.showMessage({format: 'translate', level: 'info', text: msg, params: params});
    }

    transWarn(msg, params = {}) {
        return this.showMessage({format: 'translate', level: 'warning', text: msg, params: params});
    }

    transSuccess(msg, params = {}) {
        return this.showMessage({format: 'translate', level: 'success', text: msg, params: params});
    }

    verbError(msg) {
        return this.showMessage({format: 'verb', level: 'danger', text: msg});
    }

    createScope($scope) {
        const result = new ScopedInfoPopup(this);
        if ($scope) {
            $scope.$on('$destroy', () => result.clearAll());
        }
        return result;
    }

    removeMessage(msg) {
        this.MessageBus.broadcast(InfoPopupWidget.TopicInvalidate, msg.id);
    }
}
