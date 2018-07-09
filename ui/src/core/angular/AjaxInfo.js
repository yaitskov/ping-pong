import AbstractAjaxInfo from './AbstractAjaxInfo.js';
import ScopedAjaxInfo from './ScopedAjaxInfo.js';

export default class AjaxInfo extends AbstractAjaxInfo {
    constructor(...args) {
        super(...args);
    }

    scope($scope) {
        const result = new ScopedAjaxInfo(
            this.InfoPopup.createScope(), this.InfoPopup,
            this.requestStatus, this.$http, this.auth);
        result.bind($scope);
        return result;
    }
}