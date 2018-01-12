describe('main-menu', function () {
    beforeEach(module('pingPong'));

    beforeEach(module(function ($provide) {
        $provide.value('auth', {
            myName: () => 'gost',
            userType: function () {
                console.log('mock is called');
                return 'Admin';
            }
        });
    }));

    var $controller;
    var $componentController;

    beforeEach(inject(function(_$controller_,_$componentController_) {
        $controller = _$controller_;
        $componentController = _$componentController_;
    }));

    describe('isAdmin', function () {
        it('isAdmin true if user type Admin', function () {
            var controller = $componentController('mainMenu', {});
            expect(controller.isAdmin()).toBe(true);
        });
    });
});
