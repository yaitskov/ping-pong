describe('java script', () => {
    describe('api', () => {
        describe('Map', () => {
            const m = new Map([1,2,3,4].map(i => ["key " + i, "value " + i]));
            it('find value', () => expect(m.get('key 3')).toBe('value 3'));
        });
        class X {
            static get myName() {
                return this.name;
            }
        }
        describe('map', () => {
            it('Object.assign does not merge nested maps', () => {
                expect(Object.assign({}, {a: {a: 1}}, {a: {b: 2}})).toEqual({a: {b: 2}});
            });
            it('array map', () => expect([1, 2].map(n => n * 2)).toEqual([2, 4]));
            it('object map entries', () => expect(Object.entries({1: 2}).map(([k, v]) => `${k}-${v}`)).toEqual(['1-2']));
            it('no object map', () => {
                   const m = {1: 2};
                expect(m.map).toBeUndefined();
            });
        });
        it('class static getter name reflection', () => expect(X.myName).toBe('X'));
        it('class has name field', () => expect(X.name).toBe('X'));
        function sum(a, b) { return a + b; }
        it('call function', () => expect(sum.apply(null, [1, 2])).toBe(3));
    });
    describe('syntax', () => {
        describe('function', () => {
            describe('lmabda', () => {
                it('return literal object', () => {
                    const f = () => ({x: 1});
                    expect(f()).toEqual({x: 1});
                });
            });
        });
        describe('map', () => {
            it('spread', () => {
                const m1 = {a: 1, b: 2};
                const m2 = {c: 3, b: 8};
                expect({...m1, ...m2}).toEqual({a: 1, b: 8, c: 3});
                expect(m1.b).toBe(2);
            });
            it('nested spread', () => {
                const m1 = {a: {a: 1, b: 2}, b: 2};
                const m2 = {a: {c: 3, b: 8}, c: 4};
                expect({...m1, ...m2, a: {...m1.a, ...m2.a}}).
                    toEqual({a: {a: 1, b: 8, c: 3}, b: 2, c: 4});
                expect(m1.a.b).toBe(2);
            });
        });
        describe('switch', () => {
            it('undefined literal in case', () => {
                let executed;
                switch (undefined) {
                case undefined:
                    executed = 'undefined';
                    break;
                default:
                    executed = 'default';
                }
                expect(executed).toBe('undefined');
            });
        });
    });
    describe('classes', () => {
        describe('getter', () => {
            it('static override from base', () => {
                class Base {
                    static get s() { return 'base'; }
                    readS() {
                        return this.constructor.s;
                    }
                }
                class Next extends Base {
                    static get s() { return 'next'; }
                }
                expect(new Next().readS()).toBe('next');
            });
        });
        describe('method', () => {
            class Base {
                static get s() { return [1]; }
                get g() { return 'A'; }
                f() { return 'A'; }
                static className() {
                    return this.name;
                }
            }

            class Next extends Base {
                static get s() { return [2].concat(super.s); }
                get g() { return 'N' + super.g; }
                f() { return 'B' + super.f(); }
            }

            it('static class name method', () => {
                expect(Base.className()).toBe('Base');
                expect(Next.className()).toBe('Next');
            });

            it('call overridden', () => {
                expect(new Next().f()).toBe('BA');
            });

            it('call overridden getter', () => {
                expect(new Next().g).toBe('NA');
            });

            it('call static overridden getter', () => {
                expect(Next.s).toEqual([2, 1]);
            });

            it('expand array of arguments', () => {
                function sum(a, b) { return a + b; }
                const a = [1, 2];
                expect(sum(...a)).toBe(3);
            });
            it('call method by ref', () => {
                class C {
                    constructor() {
                        this.a = 3;
                    }
                    methodF(n) {
                        return this.a + n;
                    }
                }
                const c = new C();
                const methodPointer = c.methodF.bind(c);
                expect(methodPointer(1)).toBe(4);
            });
        });
        describe('constructor', () => {
            it('inherit', () => {
                class Base {
                    constructor(a) {
                        this.a = a;
                    }
                }
                class Next extends Base {
                }

                expect(new Next(9).a).toBe(9);

            });
            it('override parent', () => {
                class Base {
                    constructor() {
                        this.base = 1;
                    }
                }

                class Next extends Base {
                    constructor() {
                        super(); // is required to access this
                        this.next = 1;
                    }
                }

                expect(new Next().next).toBe(1);
                expect(new Next().base).toBe(1);
            });
        });
    });
});
