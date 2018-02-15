import angular from 'angular';
import angularTranslate from 'angular-translate';
import manyButtonToggler from './manyButtonToggler.js';
import { setupAngularJs } from 'test/angularjs-test-setup.js';

describe('manyButtonToggler', () => {
    angular.module('test-many-button-toggler', ['pascalprecht.translate']).
        config(['$translateProvider', ($translateProvider) => {
            $translateProvider.translations(
                'en', {'input-label': 'inputLabel',
                       'A-value-label': 'A label',
                       'B-value-label': 'B label'});
            $translateProvider.preferredLanguage('en');
        }]).
        directive('manyButtonToggler', manyButtonToggler).
        component('useManyButtonToggler', {
            template: `<many-button-toggler domain="['A', 'B']" ng-model="$ctrl.model"
 selected-class="btn-primary" label="input-label"/>`,
            controller: function () {
                this.model = 'A';
            }
        });

    const ctx = setupAngularJs('use-many-button-toggler',
                               {moduleName: 'test-many-button-toggler'});

    const btnA = () => ctx.element.find('a').slice(0, 1);
    const btnB = () => ctx.element.find('a').slice(1, 2);

    it('button for value A is selected', () => {
        expect(btnA().hasClass('btn-primary')).toBeTrue();
    });

    it('button for value B is not selected', () => {
        expect(btnB().hasClass('btn-primary')).toBeFalse();
    });

    it('B is selected', () => {
        btnB().click();
        ctx.sync();
        expect(ctx.ctrl.model).toBe('B');
        expect(btnB().hasClass('btn-primary')).toBeTrue();
    });

    it('ctrl model is set', () => {
        expect(ctx.ctrl.model).toBe('A');
    });

    it('field label is translated', () => {
        expect(ctx.element.find('label').text()).toBe('inputLabel');
    });

    it('A value label is translated', () => {
        expect(btnA().text()).toBe('A label');
    });

    it('B value label is translated', () => {
        expect(btnB().text()).toBe('B label');
    });
});
