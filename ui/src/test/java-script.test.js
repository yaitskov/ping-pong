describe('java script', () => {
    describe('classes', () => {
        describe('method', () => {
            class Base {
                static get s() { return [1]; }
                get g() { return 'A'; }
                f() { return 'A'; }
            }

            class Next extends Base {
                static get s() { return [2].concat(super.s); }
                get g() { return 'N' + super.g; }
                f() { return 'B' + super.f(); }
            }

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
            }) ;
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
