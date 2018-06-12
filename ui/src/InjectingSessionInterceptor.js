import AngularBean from 'core/angular/AngularBean.js';

export default class InjectingSessionInterceptors extends AngularBean {
    static get $inject() {
        return ['auth'];
    }

    constructor(...args) {
        super(...args);
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

            return config;
        };
    }
}
