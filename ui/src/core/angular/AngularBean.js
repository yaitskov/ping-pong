import injectArgs from './injectArgs.js';

export default class AngularBean {
    static get $inject() {
        throw new Error('override - must return array of bean names');
    }

    constructor() {
        injectArgs(this, arguments);
    }

    static get readyEvent() {
        return `event.${this.name}.ready`;
    }
}
