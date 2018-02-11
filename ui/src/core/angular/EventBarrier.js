export default class EventBarrier {
    constructor(labels, callback) {
        this.labels = {};
        labels.forEach((k) => this.labels[k] = 0);
        this.callback = callback;
        this.gotCount = labels.length;
    }

    got(label, value) {
        if (!(label in this.labels)) {
            throw new Error(`Label [${label}] is not expected`);
        }
        console.log(`Label [${label}] fired`);
        const was = this.labels[label];
        this.labels[label] = 1;
        if (value) {
            this.value = value;
        }
        this.gotCount -= (1 - was);
        if (this.gotCount <= 0) {
            console.log(`Fire callback`);
            this.callback(this.value);
        }
    }
}
