import AngularBean from 'core/angular/AngularBean.js';

export default class InjectingSessionInterceptors extends AngularBean {
    static get $inject() {
        return ['auth'];
    }

    constructor(...args) {
        super(...args);
        this.action2WmId = {};
        console.log("args " + args);
        this.request = (config) => {
            const session = this.auth.mySession();
            if (session) {
                if (config.headers.session) {
                    config.headers.session = session;
                }
            } else if (config.headers.session) {
                // prevent sending request
                console.log("Error session is missing");
            }
            const wmId = this.action2WmId[config.url];
            if (wmId) {
                delete this.action2WmId[config.url];
                console.log(`Complete warm up ${config.url} => ${wmId}`);
                config.headers['cs-warm-up-id'] = wmId;
            }
            return config;
        };

        this.regActionWmId = (action, wmId) => {
            this.action2WmId[action] = wmId;
        };
    }
}
