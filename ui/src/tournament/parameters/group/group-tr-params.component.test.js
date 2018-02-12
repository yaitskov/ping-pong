import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import GroupParamsCtrl from './GroupParamsCtrl.js';

describe('group-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'group-tr-params',
        {onInit: s => s.$on(GroupParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

});
