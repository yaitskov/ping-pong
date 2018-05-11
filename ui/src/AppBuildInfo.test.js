import AppBuildInfo from 'AppBuildInfo.js';

describe('AppBuildInfo', () => {
    const abi = new AppBuildInfo();
    it('last commit hash is interpolated',
       () => expect(abi.lastCommitHash.length).toBe(40));
    it('build time is more that 0',
       () => expect(abi.buildTime.getTime()).toBeGreaterThan(1000));
    it('build time is less that now',
       () => expect(abi.buildTime.getTime()).toBeLessThan(new Date().getTime() + 1));
});
