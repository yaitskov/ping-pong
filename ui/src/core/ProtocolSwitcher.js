import AngularBean from 'core/angular/AngularBean.js';

export default class ProtocolSwitcher extends AngularBean {
    static get $inject() {
        return ['$window', 'InfoPopup'];
    }

    isHttpsOrLocal() {
        return this.$window.location.protocol.indexOf('https') == 0 ||
               this.$window.location.href.indexOf('://127.0.0.1/') > 0  ;
    }

    isHttps() {
        return this.$window.location.protocol.indexOf('https') == 0;
    }

    httpsUrl() {
        return this.$window.location.href.replace('http:', 'https:');
    }

    ifHttpsOrLocal(cb, InfoPopup = null, errorMessage = 'feature-requires-https-protocol') {
        if (this.isHttpsOrLocal()) {
            cb();
        } else {
            (InfoPopup || this.InfoPopup).transWarn(errorMessage, {'url': this.httpsUrl()});
        }
    }

    ifHttps(cb, InfoPopup = null, errorMessage = 'feature-requires-https-protocol') {
        if (this.isHttps()) {
             cb();
        } else {
             (InfoPopup || this.InfoPopup).transWarn(errorMessage, {'url': this.httpsUrl()});
        }
    }
}
