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

    static get TopicError() {
        return 'voice-input-error';
    }

    static get $inject() {
        return ['MessageBus', 'ProtocolSwitcher', 'InfoPopup'];
    }

    static get isSupported() {
        return webkitSpeechRecognition;
    }

    _createSpeechRecognition() {
        if (webkitSpeechRecognition) {
            return new webkitSpeechRecognition();
        }
        return new FakeSpeechRecognition(this.InfoPopup);
    }

    constructor(...args) {
        super(...args);
        this.working = false;
        this.speechRecognition = this._createSpeechRecognition();
        this.speechRecognition.continuous = false;
        this.speechRecognition.interim = false;
        this.speechRecognition.interimResults = false;
        this.speechRecognition.onend = (e) => {
            this.working = false;
            this.MessageBus.broadcast(this.constructor.TopicStop);
        };
        this.speechRecognition.onerror = (e) => {
            this.working = false;
            console.error("Voice error [" + e.error + "]");
            this.MessageBus.broadcast(this.constructor.TopicError, e.error);
        };
        this.speechRecognition.onaudiostart = (e) => {
            this.working = true;
        };
        this.speechRecognition.onresult = (e) => {
            if (e.results.length) {
                this.MessageBus.broadcast(this.constructor.TopicTranscripted, e.results);
            }
        };
    }

    transcriptFrom(lang) {
        this.ProtocolSwitcher.ifHttps(() => this._transcriptFrom(lang));
    }

    _transcriptFrom(lang) {
        this.speechRecognition.lang = lang || AppLang.getLanguage();
        if (this.working) {
            this.speechRecognition.stop();
        }
        this.speechRecognition.start();
    }
}
