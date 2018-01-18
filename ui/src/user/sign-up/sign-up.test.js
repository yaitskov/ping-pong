import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

describe('sign-up', () => {
    const ctx = setupAngularJs('sign-up');
    const baseFormData = {'#firstName': 'daniil', '#lastName': 'iaitskov'};

    it('dom working', () => {
        ctx.find('#firstName').val('123').triggerHandler('input');
        expect(ctx.find('#firstName').val()).toBe('123');
        expect(ctx.ctrl.firstName).toBe('123');
        ctx.ctrl.firstName = '321';
        ctx.sync();
        expect(ctx.find('#firstName').val()).toBe('321');
    });

    ij('sign-up just by name', (jsHttpBackend, $location, LocalStorage) => {
        LocalStorage.clearAll();
        spyOn($location, 'path');
        jsHttpBackend.onPostMatch(/api.anonymous.user.register/,
                                  [e => e.toEqual(jasmine.objectContaining({name: jasmine.stringMatching(/^daniil iaitskov$/),
                                                                            sessionPart: jasmine.stringMatching(/^[a-f0-9]{20}$/)})),
                                   e => e.not.toContain('phone'),
                                   e => e.not.toContain('email')]).
            respondObject({session: '123456', uid: 1, type: 'Admin'});
        ctx.findSetInput(baseFormData);
        ctx.find('form').submit();

        jsHttpBackend.flush();
        expect($location.path).toHaveBeenCalledWith('/');

        expect(LocalStorage.get('mySession')).toBe('123456');
        expect(LocalStorage.get('myUid')).toBe('1');
        expect(LocalStorage.get('myName')).toBe('daniil iaitskov');
        expect(LocalStorage.get('myEmail')).toBeNull();
        expect(LocalStorage.get('myType')).toBe('Admin');
        expect(ctx.ctrl.form.firstName.$error.required).toBeUndefined();
    });

    ij('sign-up with phone and email', (jsHttpBackend, $location, LocalStorage) => {
        LocalStorage.clearAll();
        spyOn($location, 'path');
        const email = 'daniil@gmail.com';
        jsHttpBackend.onPostMatch(/api.anonymous.user.register/,
                                  [e => e.toEqual(jasmine.objectContaining({email: jasmine.stringMatching(/^daniil@gmail.com$/),
                                                                            phone: jasmine.stringMatching(/^911$/)}))]).
            respondObject({session: '123456', uid: 1, type: 'Admin'});
        ctx.findSetInput(Object.assign({'#email': email, '#phone': '911'},
                                       baseFormData));
        ctx.find('form').submit();

        jsHttpBackend.flush();
        expect($location.path).toHaveBeenCalledWith('/');
        expect(LocalStorage.get('myEmail')).toBe(email);
    });

    ij('sign-up validation fails due empty first name', (LocalStorage) => {
        LocalStorage.clearAll();
        ctx.find('#lastName').val('iaitskov').triggerHandler('input');
        ctx.find('form').submit();
        expect(LocalStorage.get('mySession')).toBeNull();
        expect(ctx.ctrl.form.firstName.$error.required).toBeTrue();
    });

    it('phone and email optional', () => {
        ctx.find('form').submit();
        expect(ctx.ctrl.form.phone.$error.required).toBeUndefined();
        expect(ctx.ctrl.form.email.$error.required).toBeUndefined();
    });

    it('email format validation works', () => {
        ctx.find('#email').val('x@@x').triggerHandler('input');
        ctx.find('form').submit();
        expect(ctx.ctrl.form.email.$error.email).toBeTrue();
    });

    it('phone format validation works', () => {
        ctx.find('#phone').val('12341234k').triggerHandler('input');
        ctx.find('form').submit();
        expect(ctx.ctrl.form.phone.$error.phone).toBeTrue();
    });

    it('phone length validation works', () => {
        ctx.find('#phone').val('22222222222222222222222222222222222222222222222222').triggerHandler('input');
        ctx.find('form').submit();
        expect(ctx.ctrl.form.phone.$error.phone).toBeUndefined();
        expect(ctx.ctrl.form.phone.$error.maxlength).toBeTrue();
    });

    it('email length validation works', () => {
        ctx.find('#email').val('22222222222222222222222222@ssssssssssssssssssssssssssssssssssssssss.com').triggerHandler('input');
        ctx.find('form').submit();
        expect(ctx.ctrl.form.email.$error.email).toBeUndefined();
        expect(ctx.ctrl.form.email.$error.maxlength).toBeTrue();
    });
});
