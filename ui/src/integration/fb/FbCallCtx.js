export default class FbCallCtx {
    constructor(okCb = () => {}, errCb = () => {}, recoverableRetries) {
        this.extraPerms = new Set();
        this.recoverableRetries = recoverableRetries || 3;
        this.okCb = okCb;
        this.errCb = errCb;
        this.name = 'Remote call';
    }

    named(name) {
        this.name = name;
        return this;
    }

    static ofOk(okCb) {
        return new FbCallCtx(okCb);
    }

    static ofNullable(fbCallCtx) {
        return fbCallCtx ? fbCallCtx : new FbCallCtx();
    }

    preChainOkCb(preOkCb) {
        const originCb = this.okCb;
        this.okCb = (data) => {
            preOkCb(data);
            originCb(data);
        };
        return this;
    }

    wrapOkCb(okWrappingCb) {
        const originCb = this.okCb;
        this.okCb = (data) => {
            okWrappingCb(data, originCb);
        };
        return this;
    }

    retry() {
        const copy = {...this};
        copy.recoverableRetries -= 1;
        return copy;
    }

    isExhausted() {
        return this.recoverableRetries < 0;
    }
}
