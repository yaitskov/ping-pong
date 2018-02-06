
describe('jasmine features', () => {
    it('composite', () => {
        expect({a: 1, b: 'x'}).toEqual(
            jasmine.objectContaining({a: jasmine.any(Number)}));
    });
    it('asymmetric', () => {
        expect({a: 1, b: 'x'}).toEqual(
            jasmine.objectContaining({a: any.oddNumber(), b: jasmine.stringMatching(/./)}));
    });
    it('createSpyOn', () => {
        const s = jasmine.createSpyObj('s1', ['moveTo']);
        s.moveTo.and.returnValue(2);
        expect(s.moveTo).not.toHaveBeenCalled();
        expect(s.moveTo()).toBe(2);
        expect(s.moveTo).toHaveBeenCalled();
    });
    it('have called with a generic matcher', () => {
        {
            const s = jasmine.createSpyObj('s1', ['moveTo']);
            s.moveTo('hello');
            expect(s.moveTo).toHaveBeenCalledWith(jasmine.any(String));
        }

        {
            const s = jasmine.createSpyObj('s1', ['moveTo']);
            s.moveTo(123);
            expect(s.moveTo).not.toHaveBeenCalledWith(jasmine.any(String));
        }

        {
            const s = jasmine.createSpyObj('s1', ['moveTo']);
            s.moveTo({x: 123});
            expect(s.moveTo).toHaveBeenCalledWith(
                jasmine.objectContaining({x: 123}));
        }
    });
});
