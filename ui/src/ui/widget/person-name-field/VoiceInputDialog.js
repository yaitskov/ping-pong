import SimpleDialog from 'core/angular/SimpleDialog.js';
import VoiceInput from './VoiceInput.js';
import AppLang from 'ui/lang.js';

export default class VoiceInputDialog extends SimpleDialog {
    static get $inject() {
        return ['VoiceInput', 'pageCtx'].concat(super.$inject);
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
                       () => this.microphoneWorking = false);
        this.microphoneWorking = false;
        this.transcripts = null;
        this.lang = AppLang.getLanguage();
    }

    onTranscripted(results) {
        this.transcripts = [];
        for (let i = 0; results.length; ++i) {
            let row = results[i];
            for(let j = 0; j < row.length; ++j) {
                this.transcripts.push(row[j]);
            }
        }
        this.transcripts.splice(10);
    }

    _show() {
        this.turnOnMic();
        this.showDialog('voice-input-dialog');
    }

    turnOnMic() {
        this.VoiceInput.transcriptFrom(this.lang);
    }

    chooseIt(variant) {
        this.transcripts = null;
        this.send(this.constructor.TopicPick, variant);
    }
}