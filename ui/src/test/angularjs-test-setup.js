import JsHttpBackend from './JsHttpBackend.js';

export function defineAppModule(moduleName) {
    angular.module(moduleName, ['cloudSport', 'ngMock', 'cloudSportE2e.templates']);
    angular.module(moduleName).service('jsHttpBackend', JsHttpBackend);
    return moduleName;
}

defineAppModule('cloudSportE2e');

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

export function setupAngularJs(ctrlElementId, initCb, moduleName) {
    beforeEach(angular.mock.module(moduleName || 'cloudSportE2e'));

    const ctx = new Ctx();

    beforeEach(angular.mock.inject(function($rootScope, $compile, jsHttpBackend, $routeParams) {
        ctx.scope = $rootScope.$new();
        if (initCb) {
            initCb(ctx.scope, jsHttpBackend, $routeParams);
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
