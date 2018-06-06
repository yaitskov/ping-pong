import angular from 'angular';
import SimpleDialog from 'core/angular/SimpleDialog.js';

export default class ScreenSharerDialog extends SimpleDialog {
    static get $inject() {
        return ['$window', '$timeout', 'Facebook',
                'pageCtx', '$http', 'InfoPopup'].concat(super.$inject);
    }

    $onInit() {
        this.sink = 'file';
        this.screenshotData = "";
        this.subscribe(this.constructor.TopicShow,
                       (canvas) => this.setData(canvas));
    }

    static get TopicShow() {
        return 'screen-sharer-dialog-show';
    }

    get tagId() {
        return "screen-sharer-dialog";
    }

    download() {
        this.$window.location = this.screenshotData;
    }

    setData(canvas) {
        console.log("Info set canvas in dialog");
        this.screenshotData = canvas.toDataURL('png');
        this.suggestedFileName = 'Group results.png';
        this.showDialog();
    }

    useFbSink() {
        if (this.sink == 'facebook') {
            return;
        }
        this.Facebook.listPages((pageList) => {
            this.fbTargetPageId = this.pageCtx.get('last-facebook-page-to-publish') || 'me';
            this.sink = 'facebook';
            this.facebookPages = pageList;
            this.$timeout(() => this.$scope.$digest());
        });
    }

    publish(attempt) {
        this.Facebook.ensureLogin((ar) => this._publish(ar.accessToken, attempt || new Set()));
    }

    _publish(accessToken, attempt) {
        const blob = this.dataURItoBlob(this.screenshotData, 'image/png');
        const payload = new FormData();
        payload.append('access_token', accessToken);
        payload.append('source', blob);

        this.$http({
            url: `https://graph.facebook.com/${this.fbTargetPageId}/photos`,
            method: 'POST',
            data: payload,
            //assign content-type as undefined, the browser
            //will assign the correct boundary for us
            headers: {'Content-Type': undefined},
            //prevents serializing payload.  don't do it.
            transformRequest: angular.identity
        }).
            then(
                (rok) => {
                    console.log(`image posted ${JSON.stringify(rok)}`);
                    this.pageCtx.put('last-facebook-page-to-publish', this.fbTargetPageId);
                    this.InfoPopup.transInfo('screenshot posted');
                }).
            catch((e) => {
                console.log(`failed ${JSON.stringify(e)}`);
                if (e.data.error.code == 200) {
                    const sizeWas = attempt.size;
                    const msg = e.data.error.message;
                    const extraPermission = msg.substr(1 + msg.lastIndexOf(' '));
                    attempt.add(extraPermission);
                    if (attempt.size == sizeWas) {
                       this.InfoPopup.transError(
                           "permission-still-missing", {perm: extraPermission});
                       console.log("permission " + extraPermission + " is not granted");
                       return;
                    }
                    this.InfoPopup.transInfo('not enough permission', {perm: extraPermission});
                    this.Facebook.login((ar) => {
                        this._publish(ar.authResponse.accessToken, attempt);
                    }, extraPermission);
                // Error validating access token:
                // The session is invalid because the user logged out.",
                // "type":"OAuthException","code":190,"error_subcode":467
                } else if (e.data.error.code == 190) {
                    this.InfoPopup.transInfo('facebook-token-expired', e.data.error);
                    this.Facebook.login((ar) => {
                                            this._publish(ar.authResponse.accessToken, new Set());
                                        });
                } else {
                    this.InfoPopup.transError('facebook-unknown-error', e.data.error);
                }
            });
    }

    useSink(sink) {
        this.sink = sink;
    }

    dataURItoBlob(dataURI, type) {
        let byteString = atob(dataURI.split(',')[1]);
        let ab = new ArrayBuffer(byteString.length);
        let ia = new Uint8Array(ab);
        for (let i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i);
        }
        return new Blob([ia], {type: type});
    }
}
