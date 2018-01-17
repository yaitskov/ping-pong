'use strict';

angular.module('pingPongE2e', ['pingPong', 'ngMock', 'pingPongE2e.templates']);

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

    sync() {
        this.scope.$digest();
    }
}

export default function setupAngularJs(ctrlElementId) {
    beforeEach(angular.mock.module('pingPongE2e'));

    const ctx = new Ctx();

    beforeEach(angular.mock.inject(function($rootScope, $compile) {
        ctx.scope = $rootScope.$new();

        ctx.element = angular.element(`<${ctrlElementId}></${ctrlElementId}>`);
        ctx.element = $compile(ctx.element)(ctx.scope);
        ctx.scope.$digest();

        ctx.ctrl = ctx.element.controller(camelCase(ctrlElementId));
        // console.log("controller element " + ctx.element + " controller " + ctx.ctrl);
        ctx.scope.$apply();
    }));

    return ctx;
}
