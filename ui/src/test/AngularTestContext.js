export default class AngularTestContext {
    constructor() {
        this.$rootScope = null;
        this.scope = null;
        this.element = null;
        this.ctrl = null;
        this.MessageBus = null;
    }

    recordEvents(topicName, bc) {
        const events = [];
        const cleaner = this.MessageBus.subscribe(topicName, (...params) => {
            // console.log(`record event ${params[0]} in topic ${topicName}`);
            events.push(params);
        });
        try {
            bc(events);
        } finally {
            cleaner();
        }
    }

    send(...args) {
        this.MessageBus.broadcast(...args);
        this.sync();
        return args[0];
    }

    broadcast() {
        this.$rootScope.$broadcast(...arguments);
        this.sync();
    }

    toggleOn(jqueryPredicate) {
        this.element.find(jqueryPredicate).bootstrapToggle('on');
        this.sync();
    }

    toggleOff(jqueryPredicate) {
        this.element.find(jqueryPredicate).bootstrapToggle('off');
        this.sync();
    }

    btnTogglesDiffClasses(jqueryPrefix, valueF, options) {
        expect(valueF()).toBe(options.default.value);
        this.click(`${jqueryPrefix} .${options.default.clazz}`);
        expect(valueF()).toBe(options.default.value);
        this.click(`${jqueryPrefix} .btn:not(.${options.default.clazz})`);
        expect(valueF()).toBe(options.other.value);
        this.click(`${jqueryPrefix} .btn:not(.${options.other.clazz})`);
        expect(valueF()).toBe(options.default.value);
    }

    btnArrayToggles(jqueryPrefix, valueF, expectedValues) {
        const buttons = this.element.find(jqueryPrefix);
        expect(buttons.length).toBe(expectedValues.length);

        for (let i = 0; i < expectedValues.length; ++i) {
            buttons.get(i).click();
            this.sync();
            expect(valueF()).toBe(expectedValues[i]);
        }
    }

    setValue(jqueryPrefix, value) {
        this.element.find(jqueryPrefix).val(value).trigger('change');
        this.sync();
    }

    hasClass(jqueryPrefix, cssClass) {
       expect(this.element.find(jqueryPrefix).hasClass(cssClass)).toBeTrue();
    }

    hasNoClass(jqueryPrefix, cssClass) {
       expect(this.element.find(jqueryPrefix).hasClass(cssClass)).toBeFalse();
    }

    btnToggles(jqueryPrefix, valueF, options) {
        this.btnTogglesDiffClasses(jqueryPrefix, valueF, options);
    }

    click(jqueryPredicate) {
        this.element.find(jqueryPredicate).click();
        this.sync();
    }

    checked(jqueryPredicate) {
        expect(this.element.find(jqueryPredicate).prop('checked')).toBeTrue();
    }

    unchecked(jqueryPredicate) {
        expect(this.element.find(jqueryPredicate).prop('checked')).toBeFalse();
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
