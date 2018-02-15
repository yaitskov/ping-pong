export class _BackedUpValue {
    constructor(defaultF, getF, keepOnValue) {
        this.keepOnValue = keepOnValue === undefined ? true : keepOnValue;
        this.backup = null;
        this.map = (newValue, oldValue) => {
            if (newValue === undefined) {
                return getF();
            }
            if (newValue === this.keepOnValue) {
                const currentValue = getF();
                if (currentValue) {
                    return currentValue;
                }
                return this.backup ? this.backup : defaultF();
            } else {
                this.backup = getF();
                return undefined;
            }
        };
    }
}

export default function backedUpValue() {
    return new _BackedUpValue(...arguments);
}
