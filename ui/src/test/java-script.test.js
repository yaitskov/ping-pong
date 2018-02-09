describe('java script', () => {
    describe('classes', () => {
        describe('method', () => {
            class Base {
                get g() { return 'A'; }
                f() { return 'A'; }
            }

            class Next extends Base {
                get g() { return 'N' + super.g; }
                f() { return 'B' + super.f(); }
            }

            it('call overridden', () => {
                expect(new Next().f()).toBe('BA');
            });

            it('call overridden getter', () => {
                expect(new Next().g).toBe('NA');
            });
        });
        describe('constructor', () => {
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
