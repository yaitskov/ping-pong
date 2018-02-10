export default class JsHttpBackend {
    static get $inject() { return ['$httpBackend']; }
    constructor($httpBackend) {
        this.$httpBackend = $httpBackend;
    }
    onGet(url) {
        const requestHandler = this.$httpBackend.whenGET(url);
        return new function () {
            this.respondObject = (obj) => requestHandler.respond(JSON.stringify(obj));
        };
    }
    onPost(url, callback) {
        const requestHandler = this.$httpBackend.whenPOST(url, (data) => callback(JSON.parse(data)));
        return new function () {
            this.respondObject = (obj) => requestHandler.respond(
                200, JSON.stringify(obj), {'Content-Type': 'application/json'});
        };
    }
    onPostMatch(url, matchersF) {
        return this.onPost(url, (obj) => {
            for (let matcherF of matchersF) {
                matcherF(expect(obj));
            }
            return true;
        });
    }
    flush() {
        this.$httpBackend.flush();
    }
}
