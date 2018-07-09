import AngularBean from './AngularBean.js';

export default class AbstractAjaxInfo extends AngularBean {
    static get $inject() {
        return ['InfoPopup', 'requestStatus', '$http', 'auth'];
    }

    doAjax(label, ajaxCb, data, okCb, failCb) {
        if (!label) {
            label = 'Loading...';
        }
        if (!label.endsWith('...')) {
            label += '...';
        }
        const msg = this.scope.transInfo(label);

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

    doPost(label, url, data, okCb, failCb) {
        this.doAjax(
            label,
            (data, okCb, fCb) =>
                this.$http.post(url,
                                data,
                                {headers: {session: this.auth.mySession()}}).
                then(
                    (resp) => okCb(resp.data, resp),
                    fCb),
            data,
            okCb,
            failCb);
    }

    handleError(errResponse) {
       const errMsg = this.requestStatus.responseStatusToError(errResponse);
       this.scope.transError(errMsg.message, errMsg.params);
    }

    clear() {
       this.scope.clearAll();
    }
}
