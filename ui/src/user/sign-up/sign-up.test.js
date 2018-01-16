
angular.module('pingPongE2e', ['pingPong', 'ngMockE2E', 'pingPongE2e.templates']);

describe('sign-up', function () {
    var $httpBackend, createController, authRequestHandler, signUp, scope;
    beforeEach(angular.mock.module('pingPongE2e'));

    var controller;
    var scope;
    var element;

    beforeEach(angular.mock.inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        element = angular.element('<sign-up></sign-up>');
        element = $compile(element)(scope);
        scope.$digest();

        controller = element.controller('signUp');
        console.log("controller element " + element + " controller " + controller);
        scope.$apply();
    }));

    it('sign up successfully', function() {
        expect(element.find('input').val()).toBe('');
        element.find('input').val('123');
        expect(element.find('#firstName').val()).toBe('123');


            //expect(signUp.getFirstName()).toBe('dan');
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
