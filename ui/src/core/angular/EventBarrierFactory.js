import EventBarrier from './EventBarrier.js';

export default class EventBarrierFactory {
    create(labels, callback) {
        return new EventBarrier(labels, callback);
    }
}
