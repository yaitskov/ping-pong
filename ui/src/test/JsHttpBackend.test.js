import JsHttpBackend from './JsHttpBackend.js';

describe('JsHttpBackend', () => {
    it('init', () => {
        expect(new JsHttpBackend(1).$httpBackend).toBe(1);
    });
});
