import setupAngularJs from 'test/angularjs-test-setup.js';

describe('sign-up', function () {
    var ctx = setupAngularJs('sign-up');

    it('controller and dom', () => {
        expect(ctx.element.find('input').val()).toBe('');
        ctx.scope.$digest();
        ctx.scope.$digest();
        ctx.element.find('input').val('123');
        ctx.scope.$digest();
        ctx.scope.$digest();
        expect(ctx.element.find('#firstName').val()).toBe('123');
        expect(ctx.ctrl.firstName).toBe('123');
        ctx.scope.$digest();
        ctx.scope.$digest();
        ctx.ctrl.firstName = '321';
        ctx.scope.$digest();
        ctx.scope.$digest();
        expect(ctx.element.find('#firstName').val()).toBe('321');
            //expect(ctrl.label).toBe('Delete');
/*
            let scope = $rootScope.$new();
            //$httpBackend = _$httpBackend_; //$injector.get('$httpBackend');
            let signUp = $componentController('signUp', {$scope: scope});
            signUp.$onInit();
            //expect($document.find('#firstName').val()).toBe('dan');
            console.log(`signup = ${signUp}`);
            for (let [k,v] of signUp) {
                console.log(`signup.${k} = ${v}`);
            }
            $document.find('#firstName').val('dan');
            expect(signUp.getFirstName()).toBe('dan');
*/
            //let ctrl = createController();
        //$('#firstName').val('dan');
            //expect(signUp.x).toBe(123);
        //expect($('#firstName').val()).toBe('dan');
        // expect(signUp.firstName).toBe('dan');
        // $('#lastName').val('iai');
        // $('form').submit();
            //        $httpBackend.flush();

//     beforeEach(angular.mock.module('pingPong'));
//     beforeEach(() => {
//         angular.mock.inject(function(/*_$httpBackend_, */$componentController, $rootScope) {
//             scope = $rootScope.$new();
//             //$httpBackend = _$httpBackend_; //$injector.get('$httpBackend');
//             signUp = $componentController('signUp', {$scope: scope});
//             // authRequestHandler = $httpBackend.when(
//             //     'POST',
//             //     '/api/anonymous/user/register',
//             //     {name: 'dan iai',
//             //      email: '',
//             //      phone: '',
//             //      sessionPart: '123'}).
//             //     respond({data: {session: '123456', uid: 7, email: '', type: 'Admin'}});

// //            var $componentController = $injector.get('$componentController');
//             //createController = () => _$componentController_('signUp');
//         });
//     });

    // afterEach(function() {
    //     $httpBackend.verifyNoOutstandingExpectation();
    //     $httpBackend.verifyNoOutstandingRequest();
            // });

        // $httpBackend.expectPOST('/api/anonymous/user/register', (req) => {
        //
        //     return true;
        // });
        // $httpBackend.when("POST", new RegExp('.*')).respond((method, url, data) => {
        //     console.log(`uuuuuuuuuuuuuuu URL: [${url}]  / [${data}]`);
        //     return [200, {data: {session: '123456', uid: 7, email: '', type: 'Admin'}}];
        // });
            //respond(() => );
        // $httpBackend.expect('POST', /.*/, ///\/api\/anonymous\/user\/register/,
        //                     (req) => {
        //                         expect(req.name).toBe('dan iai');
        //                         return true;
        //                     }).
    });
});
