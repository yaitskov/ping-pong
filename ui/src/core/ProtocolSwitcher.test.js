import ProtocolSwitcher from './ProtocolSwitcher.js';

describe('ProtocolSwitcher', () => {
    const sutHttps = new ProtocolSwitcher({}, {});
    sutHttps.$window = {location: {href: 'https://localhost/#!bla', protocol: 'https:'}};

    const sutHttp = new ProtocolSwitcher({}, {});
    sutHttp.$window = {location: {href: 'http://localhost/#!bla', protocol: 'http:'}};

    it('isHttps returns true', () => {
        expect(sutHttps.isHttpsOrLocal()).toBeTrue();
    });

    it('isHttps returns false', () => {
        expect(sutHttp.isHttpsOrLocal()).toBeFalse();
    });

    it('httpsUrl replaces protocol', () => {
        expect(sutHttp.httpsUrl()).toBe('https://localhost/#!bla');
    });

    it('httpsUrl keeps protocol', () => {
        expect(sutHttps.httpsUrl()).toBe('https://localhost/#!bla');
    });
});
