import SimpleDialog from 'core/angular/SimpleDialog.js';
import FbCallCtx from 'integration/fb/FbCallCtx.js';

export default class ScreenSharerDialog extends SimpleDialog {
    static get $inject() {
        return ['$window', '$timeout', 'Facebook',
                'pageCtx', 'InfoPopup'].concat(super.$inject);
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
        this.Facebook.listPages(FbCallCtx.ofOk((pageList) => {
            this.fbTargetPageId = this.pageCtx.get('last-facebook-page-to-publish') || 'me';
            this.sink = 'facebook';
            this.facebookPages = pageList;
            this.$timeout(() => this.$scope.$digest());
        }));
    }

    publish() {
        if (!this.form.$valid) {
            this.form.$setSubmitted();
            return;
        }
        const blob = this.dataURItoBlob(this.screenshotData, 'image/png');
        this.Facebook.publishImage(
            this.fbTargetPageId,
            blob,
            this.caption,
            FbCallCtx.ofOk((data) => {
                 this.pageCtx.put('last-facebook-page-to-publish',
                                  this.fbTargetPageId);
                 this.caption = null;
                 this.form.$setPristine(true);
                 this.hideDialog();
            }));
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
