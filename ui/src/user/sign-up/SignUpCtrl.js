import SimpleController from 'core/angular/SimpleController.js';

export default class SignUpCtrl extends SimpleController {
    static get $inject() {
        return ['mainMenu', '$http', 'cutil', 'auth',
                'requestStatus', 'binder', '$scope', 'WarmUp'];
    }

    $onInit() {
        this.registerPath = '/api/anonymous/user/register';
        this.mainMenu.setTitle('Sign Up btn');
        this.form = {};
        this.WarmUp.warmUp(this.registerPath);
    }

    signUp(form, event) {
        event.preventDefault();
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            console.log('form is not valid');
            return;
        }
        this.requestStatus.startLoading('Registering account');
        const userName = this.firstName + ' ' + this.lastName;
        this.$http.post(this.registerPath,
                        {name: userName,
                         email: this.email,
                         phone: this.phone,
                         sessionPart: this.cutil.genUserSessionPart()
                        },
                        {'Content-Type': 'application/json'}).
            then(
                (okResp) => {
                    this.requestStatus.complete(okResp);
                    this.auth.storeSession(okResp.data.session,
                                      okResp.data.uid,
                                      userName, this.email,
                                      okResp.data.type);
                },
                (...a) => this.requestStatus.failed(...a));
    }
}
