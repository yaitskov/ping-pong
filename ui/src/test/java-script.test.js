describe('java script', () => {
    describe('classes', () => {
         describe('method', () => {
             it('call overridden', () => {
                 class Base {
                     f() { return 'A'; }
                 }

                 class Next extends Base {
                     f() { return 'B' + super.f(); }
                 }

                 expect(new Next().f()).toBe('BA');
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
         })
    });
});
