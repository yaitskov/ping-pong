import angular from 'angular';
import angularTranslate from 'angular-translate';
import manyButtonToggler from './manyButtonToggler.js';
import { setupAngularJs } from 'test/angularjs-test-setup.js';
import MessageBus from 'core/MessageBus.js';

describe('manyButtonToggler', () => {
    angular.module('test-many-button-toggler', ['pascalprecht.translate']).
        config(['$translateProvider', ($translateProvider) => {
            $translateProvider.translations(
                'en', {'input-label': 'inputLabel',
                       'input-label-A': 'A label',
                       'input-label-B': 'B label'});
            $translateProvider.preferredLanguage('en');
        }]).
        service('MessageBus', MessageBus).
        directive('manyButtonToggler', manyButtonToggler);

    describe('one selected class', () => {
        angular.module('test-many-button-toggler').
                       component('useManyButtonToggler', {
                           template: `<many-button-toggler domain="['A', 'B']" ng-model="$ctrl.model"
 selected-class="'btn-primary'" label="input-label"/>`,
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

    describe('2 selected class', () => {
        angular.module('test-many-button-toggler').
            component('useManyButtonTogglerTwoSelectedClasses', {
                template: `<many-button-toggler domain="['A', 'B']" ng-model="$ctrl.model"
 selected-class="['btn-primary', 'btn-success']" label="input-label"/>`,
                controller: function () {
                    this.model = 'A';
                }
            });

        const ctx = setupAngularJs('use-many-button-toggler-two-selected-classes',
                                   {moduleName: 'test-many-button-toggler'});

        const btnA = () => ctx.element.find('a').slice(0, 1);
        const btnB = () => ctx.element.find('a').slice(1, 2);

        it('button for value A is selected', () => {
            expect(btnA().hasClass('btn-primary')).toBeTrue();
        });

        it('button for value B is got selected', () => {
            btnB().click();
            ctx.sync();
            expect(btnA().hasClass('btn-primary')).toBeFalse();
            expect(btnB().hasClass('btn-success')).toBeTrue();
        });
    });

    describe('default selected class is btn-primay', () => {
        angular.module('test-many-button-toggler').
            component('useManyButtonTogglerDefaultSelectedClasses', {
                template: `<many-button-toggler domain="['A', 'B']" ng-model="$ctrl.model"
 label="input-label"/>`,
                controller: function () {
                    this.model = 'A';
                }
            });

        const ctx = setupAngularJs('use-many-button-toggler-default-selected-classes',
                                   {moduleName: 'test-many-button-toggler'});

        const btnA = () => ctx.element.find('a').slice(0, 1);
        const btnB = () => ctx.element.find('a').slice(1, 2);

        it('button for value A is selected', () => {
            expect(btnA().hasClass('btn-primary')).toBeTrue();
        });

        it('button for value B is got selected', () => {
            ctx.click(btnB());
            expect(btnA().hasClass('btn-primary')).toBeFalse();
            expect(btnB().hasClass('btn-primary')).toBeTrue();
        });
    });

    describe('bind to click event', () => {
        angular.module('test-many-button-toggler').
            component('useManyButtonTogglerWithBind', {
                template: `<many-button-toggler domain="['A', 'B']" ng-model="$ctrl.model"
 label="input-label" on-click="[$ctrl.callMe]"/>`,
                controller: function () {
                    this.model = 'A';
                    this.callHistory = [];
                    this.callMe = (index) => this.callHistory.push(index);
                }
            });

        const ctx = setupAngularJs('use-many-button-toggler-with-bind',
                                   {moduleName: 'test-many-button-toggler'});

        const btnA = () => ctx.element.find('a').slice(0, 1);
        const btnB = () => ctx.element.find('a').slice(1, 2);

        it('callMe is invoked', () => {
            expect(ctx.ctrl.callHistory).toEqual([]);
            ctx.click(btnB());
            ctx.click(btnA());
            expect(ctx.ctrl.callHistory).toEqual([0]);
        });
    });
});
