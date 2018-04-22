import MessageBus from './MessageBus.js';

describe('MessageBus', () => {
    it('subscribe, broadcast', () => {
        const bus = new MessageBus();
        let summator = 0;
        bus.subscribe('t1', (n) => summator += n);
        bus.broadcast('t1', 3);
        expect(summator).toBe(3);
    });

    it('broadcast, subscribe', () => {
        const bus = new MessageBus();
        let summator = 0;
        bus.broadcast('t1', 3);
        bus.subscribe('t1', (n) => summator += n);
        expect(summator).toBe(3);
    });

    it('subscribe, unsubscribe, broadcast', () => {
        const bus = new MessageBus();
        let summator = 0;
        const disposer = bus.subscribe('t1', (n) => summator += n);
        disposer();
        bus.broadcast('t1', 3);
        expect(summator).toBe(0);
    });

    it('broadcast, subscribe, unsubscribe, subscribe', () => {
        const bus = new MessageBus();
        let summator = 0;
        bus.broadcast('t1', 3);
        bus.subscribe('t1', (n) => summator += n)();
        bus.subscribe('t1', (n) => summator += n);
        expect(summator).toBe(3);
    });
});
