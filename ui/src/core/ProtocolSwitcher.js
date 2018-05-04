import AngularBean from 'core/angular/AngularBean.js';

export default class ProtocolSwitcher extends AngularBean {
    static get $inject() {
        return ['$window', 'InfoPopup'];
    }

    isHttps() {
        return this.$window.location.protocol.indexOf('https') == 0;
    }

    httpsUrl() {
        return this.$window.location.href.replace('http:', 'https:');
    }

    ifHttps(cb, errorMessage = 'feature-requires-https-protocol') {
        if (this.isHttps()) {
            cb();
        } else {
            this.InfoPopup.transWarn(errorMessage, {'url': this.httpsUrl()});
        }
    }
}
