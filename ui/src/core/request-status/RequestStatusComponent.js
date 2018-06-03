import AngularBean from 'core/angular/AngularBean.js';

export default class RequestStatusComponent extends AngularBean {
    static get $inject() {
        return ['requestStatus', '$scope', '$anchorScroll', '$timeout',
                '$route', '$translate', 'auth', 'binder', '$rootScope'];
    }

    reset() {
        this.error = {};
        this.loading = {};
    }

    scrollToError() {
        this.$timeout(function () {
            this.$anchorScroll('errorOutput');
        }, 1);
    }

    strToErr(msg) {
        return {message: msg, params: {}};
    }

    logout() {
        this.auth.logout();
    }

    $onInit() {
        this.reset();
        this.binder(this.$scope, {
            'event.request.started': (event, msg) => {
                this.reset();
                this.loading = this.requestStatus.convertMsg(msg ? msg : 'Loading');
            },
            'event.request.validation': (event, msg) => {
                this.reset();
                this.error = this.requestStatus.convertMsg(msg);
                this.scrollToError();
            },
            'event.request.failed': (event, response) => {
                this.reset();
                this.error = this.requestStatus.responseStatusToError(response);
                this.error.status = response.status;
                this.scrollToError();
            },
            'event.request.complete': (event, response) => {
                this.reset();
                this.error = 0;
            }
        });
        this.$rootScope.$broadcast('event.request.status.ready');
    }
}
