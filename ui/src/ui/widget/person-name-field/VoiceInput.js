import AngularBean from 'core/angular/AngularBean.js';
import AppLang from 'ui/lang.js';

class FakeSpeechRecognition {
    constructor(InfoPopup) {
        this.InfoPopup = InfoPopup;
    }
    stop() {
    }

    start() {
        this.InfoPopup.transError('browser-doesnt-support-speech-recognition');
        this.onend();
    }
}

export default class VoiceInput extends AngularBean {
    static get TopicTranscripted() {
        return 'voice-input-transcripted';
    }

    static get TopicStop() {
        return 'voice-input-stop';
    }

    static get TopicOnStop() {
        return 'voice-input-on-stop';
    }

    static get TopicError() {
        return 'voice-input-error';
    }

    static get $inject() {
        return ['$window', 'MessageBus', 'ProtocolSwitcher', 'InfoPopup'];
    }

    _createSpeechRecognition() {
        if (this.$window.webkitSpeechRecognition) {
            return new this.$window.webkitSpeechRecognition();
        }
        return new FakeSpeechRecognition(this.popupMsgScope);
    }

    noSpeechInfo() {
        this.popupMsgScope.transInfo('voice-recognition-no-speech');
    }

    constructor(...args) {
        super(...args);
        this.working = false;
        this.popupMsgScope = this.InfoPopup.createScope();
        this.speechRecognition = this._createSpeechRecognition();
        this.speechRecognition.continuous = false;
        this.speechRecognition.interim = false;
        this.speechRecognition.interimResults = false;
        this.speechRecognition.onend = (e) => {
            if (this.working && !this.onResultCalled) {
                this.noSpeechInfo();
            }
            this.working = false;
            this.MessageBus.broadcast(this.constructor.TopicOnStop);
        };
        this.speechRecognition.onerror = (e) => {
            this.working = false;
            console.error("Voice error [" + e.error + "]");

            switch (e.error) {
               case 'no-speech':
                  this.noSpeechInfo();
                  break;
               case 'not-allowed':
                  this.popupMsgScope.transError('voice-recognition-not-allowed');
                  break;
               default:
                  this.popupMsgScope.transInfo('voice-recognition-unknown', {error: e.error});
                  break;
            }
            this.MessageBus.broadcast(this.constructor.TopicError, e.error);
        };
        this.speechRecognition.onaudiostart = (e) => {
            this.working = true;
        };
        this.speechRecognition.onresult = (e) => {
            this.onResultCalled = e;
            if (e.results.length) {
                this.MessageBus.broadcast(this.constructor.TopicTranscripted, e.results);
            }
        };
        this.MessageBus.subscribe(this.constructor.TopicStop,
                                  () => this.stopListening());
    }

    stopListening() {
        if (this.working) {
           this.onResultCalled = true;
           this.speechRecognition.stop();
        }
    }

    normalizeLang(lang) {
        if (lang == 'en') {
            return lang + '-US';
        } else {
            return lang + "-" + lang.toUpperCase();
        }
    }

    transcriptFrom(lang) {
        this.ProtocolSwitcher.ifHttpsOrLocal(() => this._transcriptFrom(lang));
    }

    _transcriptFrom(lang) {
        this.popupMsgScope.clearAll();
        this.onResultCalled = null;
        this.speechRecognition.lang = this.normalizeLang(lang || AppLang.getLanguage());
        if (this.working) {
            this.speechRecognition.stop();
        }
        this.speechRecognition.start();
    }
}
