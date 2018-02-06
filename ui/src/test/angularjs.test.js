describe('angularjs', () => {
    describe('injection', () => {
        class ItIsMe {
            constructor() {
                console.log("ItIsMe is created");
            }
            get itIsMe() {
                return 'it is me';
            }
        }

        class DependOn {
            static get $inject() { return ['ItIsMe']; }
            constructor(ItIsMe) {
                this.ItIsMe = ItIsMe;
            }
            get itIsHim() {
                return this.ItIsMe.itIsMe;
            }
        }

        angular.module('angularTestInjection', []).
            service('ItIsMe', ItIsMe).
            service('DependOn', DependOn);

        it('xx', () => {
            angular.mock.module('angularTestInjection');
            angular.mock.inject(function (DependOn) {
                expect(DependOn.itIsHim).toBe('it is me');
            });
        });
    });
});
