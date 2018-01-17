describe('jasmine features', () => {
    it('composite', () => {
        expect({a: 1, b: 'x'}).toEqual(
            jasmine.objectContaining({a: jasmine.any(Number)}));
    });
    it('assymetric', () => {
        expect({a: 1, b: 'x'}).toEqual(
            jasmine.objectContaining({a: any.oddNumber()}));
    });
});
