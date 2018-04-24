import { setupAngularJs, defineAppModule } from 'test/angularjs-test-setup.js';
import CompositeCtrl from './CompositeCtrl.js';
import ComposableCtrl from './ComposableCtrl.js';
import eventBinder from './eventBinder.js';
import EventBarrierFactory from './EventBarrierFactory.js';
import MessageBus from 'core/MessageBus.js';

class MyComposableCtrl1 extends ComposableCtrl {
}

class MyComposableCtrl2 extends ComposableCtrl {
}

class MyCompositeCtrl extends CompositeCtrl {
    get expectedChildCtrls() {
        return [MyComposableCtrl1, MyComposableCtrl2];
    }
}

describe('CompositeCtrl', () => {
    const ajModule = 'compositeComposableTest';
    angular.module(ajModule, []);
    angular.module(ajModule).
        service('eBarier', EventBarrierFactory).
        service('MessageBus', MessageBus).
        factory('binder', () => eventBinder).
        component('testComposable1', {
            template: '<p>child1</p>',
            require: {
                parent: '^^testComposite'
            },
            controller: MyComposableCtrl1
        }).
        component('testComposable2', {
            template: '<p>child2</p>',
            require: {
                parent: '^^testComposite'
            },
            controller: MyComposableCtrl2
        }).
        component('testComposite', {
            template: '<test-composable1/><test-composable2/>',
            controller: MyCompositeCtrl
        });

    var compositeReadyEventCounter = 0;
    const ctx = setupAngularJs(
        'test-composite',
        {onInit: ($scope) => $scope.$on(MyCompositeCtrl.readyEvent,
                                        () => compositeReadyEventCounter += 1),
         moduleName: ajModule});
    it('ready event fired once all children are ready', () => {
        expect(compositeReadyEventCounter).toBe(1);
    });
});
