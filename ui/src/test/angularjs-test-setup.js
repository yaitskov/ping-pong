import JsHttpBackend from './JsHttpBackend.js';
import AngularTestContext from './AngularTestContext.js';

export function defineAppModule(moduleName) {
    angular.module(moduleName, ['cloudSport', 'ngMock', 'cloudSportE2e.templates']);
    angular.module(moduleName).service('jsHttpBackend', JsHttpBackend);
    return moduleName;
}

defineAppModule('cloudSportE2e');

function camelCase(name, separator) {
    return name.split(separator).map((word, i) => i ? word[0] + word.substr(1) : word).join('');
}

export function setupAngularJs(ctrlElementId, extra) {
    extra = extra || {};
    beforeEach(() => angular.mock.module(extra.moduleName || 'cloudSportE2e'));

    const ctx = new AngularTestContext();

    beforeEach(() => angular.mock.inject(function($rootScope, $compile, $injector) {
        ctx.$rootScope = $rootScope;
        ctx.scope = $rootScope.$new();
        if (extra.onInit) {
            const args = [ctx.scope];
            if (extra.onInit.length > 1) {
                args.push($injector.get('jsHttpBackend'));
            }
            if (extra.onInit.length > 2) {
                args.push($injector.get('$routeParams'));
            }
            extra.onInit.apply(null, args);
        }
        ctx.element = angular.element(extra.parentCtrl
                                      ? `<${extra.parentCtrl}><${ctrlElementId}/></${extra.parentCtrl}>`
                                      : `<${ctrlElementId}/>>`);
        ctx.element = $compile(ctx.element)(ctx.scope);
        ctx.scope.$digest();
        if (extra.parentCtrl) {
            ctx.parentScope = ctx.scope;
            ctx.parentElement = ctx.element;
            ctx.element = ctx.element.find(ctrlElementId);
            ctx.scope = ctx.element.isolateScope();
            ctx.parentCtrl = ctx.parentElement.controller(camelCase(extra.parentCtrl));
            ctx.parentScope.$apply();
        }
        ctx.ctrl = ctx.element.controller(camelCase(ctrlElementId));
        ctx.scope.$apply();
    }));

    return ctx;
}

export function ij(name, f) {
    it(name, angular.mock.inject(f));
}
