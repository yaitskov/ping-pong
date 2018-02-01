'use strict';

angular.module('pingPongE2e', ['pingPong', 'ngMock', 'pingPongE2e.templates']);

angular.module('pingPongE2e')
    .service('jsHttpBackend', ['$httpBackend', function ($httpBackend) {
        const self = this;
        this.onGet = (url) => {
            const requestHandler = $httpBackend.whenGET(url);
            return new function () {
                this.respondObject = (obj) => requestHandler.respond(JSON.stringify(obj));
            };
        };
        this.onPost = (url, callback) => {
            const requestHandler = $httpBackend.whenPOST(url, (data) => callback(JSON.parse(data)));
            return new function () {
                this.respondObject = (obj) => requestHandler.respond(JSON.stringify(obj));
            };
        };
        this.onPostMatch = (url, matchersF) => {
            return self.onPost(url, (obj) => {
                for (let matcherF of matchersF) {
                    matcherF(expect(obj));
                }
                return true;
            });
        };
        this.flush = () => $httpBackend.flush();
    }]);

function camelCase(name, separator) {
    return name.split(separator).map((word, i) => i ? word[0] + word.substr(1) : word).join('');
}

class Ctx {
    constructor() {
        this.scope = null;
        this.element = null;
        this.ctrl = null;
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
        this.scope.$digest();
    }
}

export function setupAngularJs(ctrlElementId, initCb) {
    beforeEach(angular.mock.module('pingPongE2e'));

    const ctx = new Ctx();

    beforeEach(angular.mock.inject(function($rootScope, $compile, jsHttpBackend) {
        ctx.scope = $rootScope.$new();
        if (initCb) {
            initCb(ctx.scope, jsHttpBackend);
        }
        ctx.element = angular.element(`<${ctrlElementId}></${ctrlElementId}>`);
        ctx.element = $compile(ctx.element)(ctx.scope);
        ctx.scope.$digest();

        ctx.ctrl = ctx.element.controller(camelCase(ctrlElementId));
        // console.log("controller element " + ctx.element + " controller " + ctx.ctrl);
        ctx.scope.$apply();
    }));

    return ctx;
}

export function ij(name, f) {
    it(name, angular.mock.inject(f));
}
