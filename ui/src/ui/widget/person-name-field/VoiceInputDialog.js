import SimpleDialog from 'core/angular/SimpleDialog.js';
import VoiceInput from './VoiceInput.js';
import AppLang from 'ui/lang.js';

export default class VoiceInputDialog extends SimpleDialog {
    static get $inject() {
        return ['$timeout', 'VoiceInput', 'pageCtx'].concat(super.$inject);
    }

    static get TopicShow() {
        return 'voice-input-dialog-show';
    }

    static get TopicPick() {
        return 'voice-input-dialog-pick';
    }

    get tagId() {
        return 'voice-input-dialog';
    }

    $onInit() {
        console.log("voice input dialog init")
        this.subscribe(this.constructor.TopicShow, () => this._show());
        this.subscribe(VoiceInput.TopicTranscripted,
                       (results) => this.onTranscripted(results));
        this.subscribe(VoiceInput.TopicOnStop,
                       () => this._stop());
        this.microphoneWorking = false;
        this.transcripts = null;
        this.lang = this.pageCtx.get('voice-recognition-language') || AppLang.getLanguage();
        this.$scope.$watch('$ctrl.lang', (newv, oldv) => {
            console.log("chose lang " + newv + " in favor " + oldv);
            if (newv) {
                this.pageCtx.put('voice-recognition-language', newv);
            }
        });
        this.onHide(() => this._onHide());
    }

    _stop() {
        this.microphoneWorking = false;
        this.$timeout(() => this.$scope.$digest());
    }

    onTranscripted(results) {
        this.transcripts = [];
        for (let i = 0; i < results.length; ++i) {
            let row = results[i];
            for(let j = 0; j < row.length; ++j) {
                this.transcripts.push(row[j].transcript);
            }
        }
        this.transcripts.splice(10);
        this.$timeout(() => this.$scope.$digest());
    }

    _onHide() {
        this.stopRecognition();
        this.send(VoiceInput.TopicClearNotifications);
        this.$timeout(() => this.$scope.$digest());
    }

    stopRecognition() {
        this.send(VoiceInput.TopicStop);
    }

    _show() {
        this.turnOnMic();
        //this.transcripts = ['HEllo world'];
        this.showDialog();
    }

    turnOnMic() {
        this.microphoneWorking = true;
        this.VoiceInput.transcriptFrom(this.lang);
    }

    hide() {
        this.hideDialog();
    }

    chooseIt(variant) {
        this.transcripts = null;
        this.hide();
        this.send(this.constructor.TopicPick, variant);
    }
}