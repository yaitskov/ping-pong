import angular from 'angular';

            // 'node_modules/angular/angular.js',
import mocks from  'angular-mocks/angular-mocks.js';

describe('main-menu', function () {
    beforeEach(module('mainMenu'));

    beforeEach(module(function ($provide) {
        $provide.value('auth', {
            userType: function () {
                console.log('mock is called');
                return 'Admin';
            }
        });
    }));

    var $controller;

    beforeEach(inject(function(_$controller_) {
        $controller = _$controller_;
    }));

    describe('isAdmin', function () {
        it('isAdmin true if user type Admin', function () {
            var controller = $controller('mainMenu',{});
            expect(controller.isAdmin()).toBe(true);
        });
    });
});
