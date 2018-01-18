import setupAngularJs from 'test/angularjs-test-setup.js';

const inject = angular.mock.inject;

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

    it('sign-up just by name', inject((jsHttpBackend, $location, LocalStorage) => {
        LocalStorage.clearAll();
        spyOn($location, 'path');
        jsHttpBackend.onPostMatch(/api.anonymous.user.register/,
                                  [e => e.toEqual(jasmine.objectContaining({name: jasmine.stringMatching(/^daniil iaitskov$/),
                                                                            sessionPart: jasmine.stringMatching(/^[a-f0-9]{20}$/)})),
                                   e => e.not.toContain('phone'),
                                   e => e.not.toContain('email')]).
            respondObject({session: '123456', uid: 1, type: 'Admin'});
        ctx.find('#firstName').val('daniil').triggerHandler('input');
        ctx.find('#lastName').val('iaitskov').triggerHandler('input');
        ctx.find('form').submit();

        jsHttpBackend.flush();
        expect($location.path).toHaveBeenCalledWith('/');

        expect(LocalStorage.get('mySession')).toBe('123456');
        expect(LocalStorage.get('myUid')).toBe('1');
        expect(LocalStorage.get('myName')).toBe('daniil iaitskov');
        expect(LocalStorage.get('myEmail')).toBeNull();
        expect(LocalStorage.get('myType')).toBe('Admin');
    }));

    it('sign-up validation fails due empty first name', inject((LocalStorage) => {
        LocalStorage.clearAll();
        ctx.find('#lastName').val('iaitskov').triggerHandler('input');
        ctx.find('form').submit();
        expect(LocalStorage.get('mySession')).toBeNull();
    }));
});
