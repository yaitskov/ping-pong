import AngularBean from 'core/angular/AngularBean.js';

export default class Facebook extends AngularBean {
    static get $inject() {
        return ['$window', 'InfoPopup', 'ProtocolSwitcher'];
    }

    constructor(...args) {
        super(...args);
    }

    api(path, cb) {
        this.$window.FB.api(path, cb);
    }

    publishImage(nodeId, blob) {
    }

    login(cb, scope) {
        this.ProtocolSwitcher.ifHttps(
            () => this.$window.FB.login(
                (r) => {
                    if (r.status == 'connected') {
                        cb(r);
                    } else {
                        console.log("login error response "
                                    + JSON.stringify(r));
                        this.InfoPopup.transError(
                            'CS not get access to your fb account', r.error);
                    }
                },
                {scope: scope || 'manage_pages publish_pages publish_actions'}));
    }

    listPages(cb) {
        this.ensureLogin((ar) => this._listPages(ar.userID, cb));
    }

    _listPages(userId, cb) {
        this.api(`/${userId}/accounts`, (r) =>
                 this.handleError(r, (data) => cb(
                     data.map(page => ({name: page.name, id: page.id})))));
    }

    ensureLogin(cb) {
        this.loginStatus((r) => {
            if (r.status == 'connected') {
                if (r.authResponse && r.authResponse.userID) {
                    cb(r.authResponse);
                } else {
                    this.InfoPopup.verbError('Facebook did not give user Id');
                }
            } else {
                this.InfoPopup.transInfo('facebook-authentication-required');
                this.login((ar) => cb(ar.authResponse));
            }
        });
    }

    unknownError(error) {
        this.InfoPopup.transError('facebook-unknown-error', error);
    }

    handleError(response, nextCb) {
        if (response.error) {
            switch (response.error.type) {
            case 'OAuthException':
                this.InfoPopup.transInfo('facebook-not-authenticated');
                break;
            default:
                this.unknownError(response.error);
            }
        } else {
            nextCb(response.data);
        }
    }

    loginStatus(cb) {
        this.$window.FB.getLoginStatus(
            (r) => {
                console.log("login status " + JSON.stringify(r));
                cb(r);
            });
    }

    checkPermissions(userId, cb) {
        this.$window.FB.api(
            `/${userId}/permissions`,
            (r) => {
                console.log("check permission " + JSON.stringify(r));
                cb(r);
            });
    }
}
