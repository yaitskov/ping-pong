import angular from 'angular';
import AngularBean from 'core/angular/AngularBean.js';
import FbCallCtx from './FbCallCtx.js';

export default class Facebook extends AngularBean {
    static get $inject() {
        return ['$http', '$window', 'InfoPopup', 'ProtocolSwitcher'];
    }

    constructor(...args) {
        super(...args);
        this.defaultFbScope = 'manage_pages publish_pages';
        this.authErrScope = this.InfoPopup.createScope();
    }

    tokenExpiredMeanwhile(err) {
        return err.code == 190 && err.error_subcode == 467;
    }

    lackOfPermission(err) {
        return err.code == 200;
    }

    reRequirePerms(r, retryCb, fbCallCtx) {
        const sizeWas = fbCallCtx.extraPerms.size;
        const msg = r.error.message;
        const extraPermission = msg.substr(1 + msg.lastIndexOf(' '));
        fbCallCtx.extraPerms.add(extraPermission);
        if (fbCallCtx.extraPerms.size == sizeWas) {
            console.log(`permission ${extraPermission} is not granted`);
            this.authErrScope.transError(
                "permission-still-missing", {perm: extraPermission});
            fbCallCtx.errCb(r.error);
            return;
        }
        this.authErrScope.transInfo('not enough permission', {perm: extraPermission});
        this.login((ar) => retryCb(), extraPermission);
    }

    handleError(r, retryCb, fbCallCtx) {
        if (r.error) {
            if (fbCallCtx.isExhausted() < 0) {
                this.InfoPopup.transError('Failing remote call exhausted all attempts');
                fbCallCtx.errCb(r.error);
                return;
            }
            if (this.lackOfPermission(r.error)) {
                this.reRequirePerms(r, retryCb, fbCallCtx);
                return;
            }
            switch (r.error.type) {
            case 'OAuthException':
                if (this.tokenExpiredMeanwhile(r.error)) {
                    this.authErrScope.transInfo('facebook-token-expired');
                    this.login((ar) => retryCb());
                } else {
                    fbCallCtx.errCb(r.error);
                    this.unknownError(r.error);
                }
                break;
            default:
                fbCallCtx.errCb(r.error);
                this.unknownError(r.error);
            }
        } else {
            fbCallCtx.okCb(r.data || r);
        }
    }

    api(path, fbCallCtx) {
        const doingMsg = this.InfoPopup.transInfo(fbCallCtx.name || 'doing fb call...');
        const nextAttempt = fbCallCtx.retry();
        this.$window.FB.api(
            path,
            (r) => {
                this.InfoPopup.removeMessage(doingMsg);
                this.handleError(
                    r,
                    () => this.api(path, nextAttempt),
                    fbCallCtx);
            });
    }

    publishImage(targetId, blob, caption, fbCallCtx) {
        const nextAttempt = fbCallCtx.retry();
        this.ensureLogin(
            (ar) => this._publish(ar.accessToken, targetId, blob,
                                  () => this.publishImage(targetId, blob, caption, nextAttempt),
                                  fbCallCtx, caption));
    }

    _publish(accessToken, targetPageId, blob, retryCb, fbCallCtx, caption) {
        const payload = new FormData();
        payload.append('access_token', accessToken);
        payload.append('source', blob);
        if (caption) {
            payload.append('caption', caption);
        }
        const doingMsg = this.InfoPopup.transInfo('publishing image on fb...');
        this.$http({
            url: `https://graph.facebook.com/${targetPageId}/photos`,
            method: 'POST',
            data: payload,
            //assign content-type as undefined, the browser
            //will assign the correct boundary for us
            headers: {'Content-Type': undefined},
            //prevents serializing payload.  don't do it.
            transformRequest: angular.identity
        }).
            then(
                (r) => {
                    this.InfoPopup.removeMessage(doingMsg);
                    this.handleError(r, retryCb,
                                     fbCallCtx.wrapOkCb((data, forwardCb) => {
                                         console.log(`image posted ${JSON.stringify(r)}`);
                                         this.authErrScope.clearAll();
                                         this.getLink(data.post_id,
                                                      FbCallCtx.ofOk((link) => {
                                                          this.InfoPopup.transInfo(
                                                              'screenshot posted',
                                                              {url: link.link});
                                                      }));
                                         forwardCb(data);
                                     }));
                }).
            catch((e) => {
                this.InfoPopup.removeMessage(doingMsg);
                console.log(`Failed ${JSON.stringify(e)}`);
                this.handleError(e.data || {error: {code: 777}},
                                 retryCb, fbCallCtx);
            });
    }

    login(cb, scope) {
        this.ProtocolSwitcher.ifHttps(
            () => {
                const doingMsg = this.InfoPopup.transInfo('authenticating on fb...');
                this.$window.FB.login(
                    (r) => {
                        this.InfoPopup.removeMessage(doingMsg);
                        if (r.status == 'connected') {
                            this.authErrScope.clearAll();
                            cb(r);
                        } else {
                            console.log("login error response "
                                        + JSON.stringify(r));
                            this.authErrScope.transError(
                                'CS not get access to your fb account', r.error);
                        }
                    },
                    {scope: scope || this.defaultFbScope});
            });
    }

    listPages(fbCallCtx) {
        this.ensureLogin((ar) => this._listPages(ar.userID, fbCallCtx.named('listing fb pages...')));
    }

    _listPages(userId, fbCallCtx) {
        this.api(`/${userId}/accounts`,
                 fbCallCtx.wrapOkCb(
                     (data, forwardCb) => forwardCb(data.map(page => ({name: page.name, id: page.id})))));
    }

    ensureLogin(cb) {
        this.loginStatus((r) => {
            if (r.status == 'connected') {
                if (r.authResponse && r.authResponse.userID) {
                    cb(r.authResponse);
                } else {
                    this.authErrScope.verbError('Facebook did not give user Id');
                }
            } else {
                this.authErrScope.transInfo('facebook-authentication-required');
                this.login((ar) => cb(ar.authResponse));
            }
        });
    }

    unknownError(error) {
        this.authErrScope.transError('facebook-unknown-error', error);
    }

    loginStatus(cb) {
        this.$window.FB.getLoginStatus(
            (r) => {
                console.log("login status " + JSON.stringify(r));
                cb(r);
            });
    }

    getLink(postId, fbCallCtx) {
        this.api(`/${postId}/?fields=link`,
                 fbCallCtx.named('resolving photo FB url...'));
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
