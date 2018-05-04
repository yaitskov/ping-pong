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

    $onInit() {
        console.log("voice input dialog init")
        this.subscribe(this.constructor.TopicShow, () => this._show());
        this.subscribe(VoiceInput.TopicTranscripted,
                       (results) => this.onTranscripted(results));
        this.subscribe(VoiceInput.TopicStop,
                       () => this._stop());
        this.microphoneWorking = false;
        this.transcripts = null;
        this.lang = AppLang.getLanguage();
        if (this.lang == 'en') {
            this.lang += '-US';
        } else {
            this.lang += "-" + this.lang.toUpperCase();
        }
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

    _show() {
        this.turnOnMic();
        //this.transcripts = ['HEllo world'];
        this.showDialog('voice-input-dialog');
    }

    turnOnMic() {
        this.microphoneWorking = true;
        this.VoiceInput.transcriptFrom(this.lang);
    }

    hide() {
        this.hideDialog('voice-input-dialog');
    }

    chooseIt(variant) {
        this.transcripts = null;
        this.hide();
        this.send(this.constructor.TopicPick, variant);
    }
}