import injectArgs from 'angular-di.js';

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

        it('inject class dependency', () => {
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

            angular.mock.module('angularTestInjection');
            angular.mock.inject(function (DependOn) {
                expect(DependOn.itIsHim).toBe('it is me');
            });
        });

        it('inject with injectArgs', () => {
            class DependOn {
                static get $inject() { return ['ItIsMe']; }
                constructor() {
                    injectArgs(this, arguments);
                }
                get itIsHim() {
                    return this.ItIsMe.itIsMe;
                }
            }

            angular.module('angularInjectArgs', []).
                    service('ItIsMe', ItIsMe).
                    service('DependOn', DependOn);
            angular.mock.module('angularInjectArgs');
            angular.mock.inject(function (DependOn) {
                expect(DependOn.itIsHim).toBe('it is me');
            });
        });

        it('injectArgs fails due number of dependencies mismatch', () => {
            class C {
                static get $inject() {
                    return ['A', 'B'];
                }
                constructor() {
                    injectArgs(this, arguments);
                }
            }

            expect(() => new C()).toThrow(new Error(
                "Mismatch between dependencies names"
                    + " and dependencies slots in class: [C]"));
        });

        it('injectArgs fails due dependency is null', () => {
            class C {
                static get $inject() {
                    return ['A', 'B'];
                }
                constructor() {
                    injectArgs(this, arguments);
                }
            }

            expect(() => new C(null, undefined)).toThrow(new Error(
                "Dependency [A] of class [C] is [null]"));
        });
    });
});
