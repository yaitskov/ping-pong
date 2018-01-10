const supportedLanguages = ['en', 'pl'];

export default class AppLang {
    static getLanguage() {
        return this.getSelectedLanguage() || this.browserLanguage();
    }

    static reset() {
        this.setLanguage(null);
    }

    static getSelectedLanguage() {
        return localStorage.getItem('myLang');
    }

    static setLanguage(lang) {
        const selected = this.getSelectedLanguage();
        if (selected == lang) {
            console.log("Language " + lang + " is already selected");
            return;
        }
        localStorage.setItem('myLang', lang);
        window.location.reload();
    }

    static browserLanguage() {
        if (window.navigator) {
            if (window.navigator.languages) {
                for (let l of window.navigator.languages) {
                    if (supportedLanguages.indexOf(l) >= 0) {
                        console.log("Pick language " + l);
                        return l;
                    }
                }
            }
            if (window.navigator.language && supportedLanguages.indexOf(window.navigator.language) >= 0) {
                console.log("Pick language " + window.navigator.language);
                return window.navigator.language;
            }
        }
        console.log("Pick default language en");
        return 'en';
    }
}
