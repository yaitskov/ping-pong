export default class AngularTestContext {
    constructor() {
        this.$rootScope = null;
        this.scope = null;
        this.element = null;
        this.ctrl = null;
    }

    broadcast() {
        this.$rootScope.$broadcast(...arguments);
        this.sync();
    }

    _visible(jqueryPredicate, yes) {
        expect(this.element.find(jqueryPredicate).hasClass('ng-hide')).toBe(!yes);
    }

    visible(jqueryPredicate) {
        this._visible(jqueryPredicate, true);
    }

    hidden(jqueryPredicate) {
        this._visible(jqueryPredicate, false);
    }

    find(q) {
        return this.element.find(q);
    }

    findSetInput(m) {
        for (let [anchor, val] of Object.entries(m)) {
            this.find(anchor).val(val).triggerHandler('input');
        }
    }

    sync() {
        if (this.parentScope) {
            this.parentScope.$digest();
        } else {
            this.scope.$digest();
        }
    }
}
