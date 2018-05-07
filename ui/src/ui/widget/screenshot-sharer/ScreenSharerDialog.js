import SimpleDialog from 'core/angular/SimpleDialog.js';

export default class ScreenSharerDialog extends SimpleDialog {
    static get $inject() {
        return ['$window', '$timeout'].concat(super.$inject);
    }

    $onInit() {
        this.screenshotData = ""; //data:image/png;base64,";

//           + " iVBORw0KGgoAAAANSUhEUgAAAAUA"
//           + "AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO"
//           + "9TXL0Y4OHwAAAABJRU5ErkJggg==";

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
        //this.$timeout(() => this.$scope.$digest());
        //var escapedBase64Data = dataUrl.replace("data:image/png;base64,","");
    }
}