import angular from 'angular';
import eventBinder from './eventBinder.js';

describe('eventBinder', () => {
    angular.module('testEventBinder', []);
    angular.mock.module('testEventBinder');
    it('bind listeners', angular.mock.inject($rootScope => {
        const $scope = $rootScope.$new();
        var event1Fired = 0;
        const cleaners = eventBinder($scope, {'event1': () => event1Fired += 1});
        expect(cleaners.length).toBe(1);
        expect(event1Fired).toBe(0);
        $rootScope.$broadcast('event1');
        expect(event1Fired).toBe(1);
        $scope.$emit('$destroy');
        $rootScope.$broadcast('event1');
        expect(event1Fired).toBe(1);
    }));

    it('returns cleaners', angular.mock.inject($rootScope => {
        const $scope = $rootScope.$new();
        var event1Fired = 0;
        const cleaners = eventBinder($scope, {'event1': () => event1Fired += 1});
        $rootScope.$broadcast('event1');
        expect(event1Fired).toBe(1);
        cleaners[0]();
        $rootScope.$broadcast('event1');
        expect(event1Fired).toBe(1);
    }));
});
