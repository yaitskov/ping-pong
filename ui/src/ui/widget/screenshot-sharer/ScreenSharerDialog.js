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
            const fbPageId = this.pageCtx.get('last-facebook-page-to-publish');
            this.sink = 'facebook';
            this.facebookPages = pageList;
            if (pageList.findIndex((pl) => pl.id == fbPageId) >= 0) {
                this.fbTagetPageId = fbPageId;
            } else {
                this.fbTagetPageId = null;
            }
            this.$timeout(() => this.$scope.$digest());
        });
    }

    publish() {
        this.Facebook.ensureLogin((ar) => this._publish(ar.accessToken));
    }

    _publish(accessToken) {
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
                    this.InfoPopup.transInfo('screenshort posted');
                }).
            catch((e) => {
                console.log(`failed ${JSON.stringify(e)}`);
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
