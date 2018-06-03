import AngularBean from './AngularBean.js';

export default class AjaxInfo extends AngularBean {
    static get $inject() {
        return ['InfoPopup', 'requestStatus'];
    }

    constructor(...args) {
        super(...args);
        this.scope = this.InfoPopup.createScope();
    }

    doAjax(label, ajaxCb, data, okCb, failCb) {
        this.scope.clearAll();
        this.scope.transInfo(label || 'Loading');

        ajaxCb(
            data,
            (r) => {
                this.scope.clearAll();
                okCb(r);
            },
            failCb ? (e) => { this.scope.clearAll(); failCb(e); } : (e) => this.handleError(e));
    }

    handleError(errResponse) {
       this.scope.clearAll();
       const errMsg = this.requestStatus.responseStatusToError(errResponse);
       this.scope.transError(errMsg.message, errMsg.params);
    }
}