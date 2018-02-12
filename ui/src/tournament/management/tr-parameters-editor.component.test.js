import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

describe('tr-parameters-editor', () => {
    const rulesF = () => { return {group: {console: 'NO'}}; };
    const tournamentF = () => { return {tid: 1}; };

    const ctx = setupAngularJs(
        'tr-parameters-editor',
        {onInit: (scope, jsHttpBackend, $routeParams) => {
            $routeParams.tournamentId = 1;
            jsHttpBackend.onGet(/api.tournament.rules.1/).
                respondObject(rulesF());
            jsHttpBackend.onGet(/api.tournament.mine.1/).
                respondObject(tournamentF());
        }});

    ij('set rules event is emitted', ($rootScope, jsHttpBackend) => {
        const broadcastSpy = spyOn($rootScope, '$broadcast');
        jsHttpBackend.flush();
        expect(broadcastSpy).toHaveBeenCalledWith(
            'event.tournament.rules.set',
            jasmine.objectContaining({tid: 1,
                                      rules: {group: {console: 'NO'}}}));
    });
});
