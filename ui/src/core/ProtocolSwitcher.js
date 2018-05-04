import AngularBean from 'core/angular/AngularBean.js';

export default class ProtocolSwitcher extends AngularBean {
    static get $inject() {
        return ['$window', 'InfoPopup'];
    }

    isHttpsOrLocal() {
        return this.$window.location.protocol.indexOf('https') == 0 ||
               this.$window.location.href.indexOf('://127.0.0.1/') > 0  ;
    }

    httpsUrl() {
        return this.$window.location.href.replace('http:', 'https:');
    }

    ifHttpsOrLocal(cb, errorMessage = 'feature-requires-https-protocol') {
        if (this.isHttpsOrLocal()) {
            cb();
        } else {
            this.InfoPopup.transWarn(errorMessage, {'url': this.httpsUrl()});
        }
    }
}
