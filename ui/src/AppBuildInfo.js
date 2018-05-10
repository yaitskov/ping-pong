export default class AppBuildInfo {
    constructor() {
        this.lastCommitHash = '$$lastCommitHash$$';
        this.buildTime = new Date(+$$buildTime$$);
    }
}
