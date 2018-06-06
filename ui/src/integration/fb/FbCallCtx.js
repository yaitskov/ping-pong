export default class FbCallCtx {
    constructor(okCb = () => {}, errCb = () => {}, recoverableRetries) {
        this.extraPerms = new Set();
        this.recoverableRetries = recoverableRetries || 3;
        this.okCb = okCb;
        this.errCb = errCb;
        this.name = 'Remote call';
    }

    static ofOk(okCb) {
        return new FbCallCtx(okCb);
    }

    static ofNullable(fbCallCtx) {
        return fbCallCtx ? fbCallCtx : new FbCallCtx();
    }

    chainOkCb(nextOkCb) {
        const originCb = this.okCb;
        this.okCb = (data) => {
            originCb(data);
            nextOkCb(data);
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
        this.recoverableRetries -= 1;
        return this;
    }

    isExhausted() {
        return this.recoverableRetries < 0;
    }
}
