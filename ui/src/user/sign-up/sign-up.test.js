import setupAngularJs from 'test/angularjs-test-setup.js';

describe('sign-up', () => {
    const ctx = setupAngularJs('sign-up');

    it('dom working', () => {
        ctx.find('#firstName').val('123').triggerHandler('input');
        expect(ctx.find('#firstName').val()).toBe('123');
        expect(ctx.ctrl.firstName).toBe('123');
        ctx.ctrl.firstName = '321';
        ctx.sync();
        expect(ctx.find('#firstName').val()).toBe('321');
    });

    it('sign-up just by name', angular.mock.inject((jsHttpBackend, $location) => {
        spyOn($location, 'path');
        jsHttpBackend.onPostMatch(/api.anonymous.user.register/,
                                  [e => e.toEqual(jasmine.objectContaining({name: jasmine.stringMatching(/^daniil iaitskov$/),
                                                                            sessionPart: jasmine.stringMatching(/^[a-f0-9]{20}$/)}))]).
            respondObject({session: '123456', uid: 1, type: 'Admin'});
        ctx.find('#firstName').val('daniil').triggerHandler('input');
        ctx.find('#lastName').val('iaitskov').triggerHandler('input');
        ctx.find('form').submit();

        jsHttpBackend.flush();
        expect($location.path).toHaveBeenCalledWith('/');
    }));

});
