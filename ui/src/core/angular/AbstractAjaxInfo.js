import AngularBean from './AngularBean.js';

export default class AbstractAjaxInfo extends AngularBean {
    static get $inject() {
        return ['InfoPopup', 'requestStatus'];
    }

    doAjax(label, ajaxCb, data, okCb, failCb) {
        this.scope.clearAll();
        const msg = this.scope.transInfo(label || 'Loading...');

        ajaxCb(
            data,
            (r) => {
                this.scope.removeMessage(msg);
                okCb(r);
            },
            (e) => {
               this.scope.removeMessage(msg);
               if (failCb) {
                   failCb(e, () => this.handleError(e));
               } else {
                   this.handleError(e);
               }
            });
    }

    handleError(errResponse) {
       const errMsg = this.requestStatus.responseStatusToError(errResponse);
       this.scope.transError(errMsg.message, errMsg.params);
    }

    clear() {
       this.scope.clearAll();
    }
}
