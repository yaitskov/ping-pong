describe('java script', () => {
    describe('classes', () => {
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
