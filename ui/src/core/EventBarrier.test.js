import EventBarrier from './EventBarrier.js';

describe('EventBarrier', () => {
    describe('single label', () => {
        it('unexpected label throws exception', () => {
            const eb = new EventBarrier(['a'], () => new Error('dead code'));
            expect(() => eb.got('b')).toThrow(new Error('Label [b] is not expected'));
        });
        it('callback is invoked', () => {
            var x = 0;
            const eb = new EventBarrier(['a'], () => x = 1);
            eb.got('a');
            expect(x).toBe(1);
        });
        it('callback receives argument', () => {
            var x = 0;
            const eb = new EventBarrier(['a'], (arg) => x = arg);
            eb.got('a', 8);
            expect(x).toBe(8);
        });
        it('callback argument is kept until overridden', () => {
            var x = 0;
            const eb = new EventBarrier(['a'], (arg) => x += arg);
            eb.got('a', 1);
            eb.got('a');
            expect(x).toBe(2);
            eb.got('a', 3);
            eb.got('a');
            expect(x).toBe(8);
        });
    });
    describe('2 labels', () => {
        it('callback is not invoked', () => {
            var x = 0;
            const eb = new EventBarrier(['a', 'b'], () => x = 1);
            eb.got('a');
            eb.got('a');
            eb.got('a');
            expect(x).toBe(0);
        });
        it('callback is invoked once all labels happenend', () => {
            var x = 0;
            const eb = new EventBarrier(['a', 'b'], () => x = 1);
            eb.got('a');
            eb.got('b');
            expect(x).toBe(1);
        });
        it('callback is invoked multiple time', () => {
            var x = 0;
            const eb = new EventBarrier(['a', 'b'], () => x += 1);
            eb.got('a');
            eb.got('b');
            eb.got('a');
            expect(x).toBe(2);
        });
    });
});
