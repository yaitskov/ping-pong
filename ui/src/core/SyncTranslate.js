import AngularBean from 'core/angular/AngularBean.js';

export default class SyncTranslate extends AngularBean {
    static get $inject() {
        return ['$translate', '$q'];
    }

    constructor(...args) {
        super(...args);
        this.lastCallId = new Object();
    }


    callTranslate(originMessage) {
        if (typeof originMessage == "string") {
            return this.$translate(originMessage);
        } else {
            return this.$translate(originMessage[0], originMessage[1]);
        }
    }

    adaptMenuMap(map) {
        for (let key of Object.keys(map)) {
            const label = map[key];
            if (typeof label == "string") {
                map[key] = {text: label};
            }
        }
    }

    transMenu(map, nextCallback) {
        this.adaptMenuMap(map);
        var callId = new Object();
        this.lastCallId = callId;
        var keys = Object.keys(map);
        var origins = [];
        for (var i = 0; i < keys.length; ++i) {
            origins.push(map[keys[i]].text);
        }
        this.$translate(origins).then((translations) => {
            if (this.lastCallId == callId) {
                for (var i = 0; i < keys.length; ++i) {
                    map[keys[i]].text = translations[map[keys[i]].text];
                }
                nextCallback(map);
            } else {
                console.log("Reject obsolete translations");
            }
        }).catch((msg) => {
            console.error(`Failed menu translation of [${JSON.stringify(map)}]`);
            nextCallback(msg);
        });;
    }

    trans(originMessage, nextCallback) {
        var callId = new Object();
        this.lastCallId = callId;
        this.callTranslate(originMessage).then((msg) => {
            if (this.lastCallId == callId) {
                nextCallback(msg);
            } else {
                console.log("Reject obsolete translation: " + msg);
            }
        }).catch((msg) => {
            console.error(`Failed translation of [${msg}]`);
            nextCallback(msg);
        });
    }

    transTitleAndMenu(originTitle, originMenu, nextCallback) {
        this.adaptMenuMap(originMenu);
        var callId = new Object();
        this.lastCallId = callId;
        var keys = Object.keys(originMenu);
        var origins = [];
        for (var i = 0; i < keys.length; ++i) {
            origins.push(originMenu[keys[i]].text);
        }
        this.$q.all([this.callTranslate(originTitle).$promise,
                     this.$translate(origins).$promise]).then(
            (responses) => {
                this.translations = responses[1];
                if (this.lastCallId == callId) {
                    for (var i = 0; i < keys.length; ++i) {
                        originMenu[keys[i]].text = this.translations[originMenu[keys[i]].text];
                    }
                    nextCallback(responses[0], originMenu);
                } else {
                    console.log("Reject obsolete translations");
                }
            });
    }
}
