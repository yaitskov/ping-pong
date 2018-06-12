import AngularBean from 'core/angular/AngularBean.js';

export default class WarmUp extends AngularBean {
    static get $inject() {
        return ['$http', 'injectingSessionInterceptor'];
    }

    warmUp(action) {
        this.$http.post(
            '/api/warm/up',
            {action: action, clientTime: new Date()},
            {headers: {session: 1}}).then(
                (ok) => {
                    if (ok.data) {
                        this.injectingSessionInterceptor.regActionWmId(action, ok.data);
                    }
                },
                (...a) => {
                    console.error("WarmUp failed " + JSON.stringify(a));
                }
            );
    }
}
