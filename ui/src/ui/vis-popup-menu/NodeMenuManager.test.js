import NodeMenuManager from './NodeMenuManager.js';

describe('NodeMenuManager', () => {
    const mgr = new NodeMenuManager(null, jasmine.createSpyObj('network', ['on']));

    describe('updatePositions', () => {
        it('moveTo is called', () => {
            const node = jasmine.createSpyObj('node', ['moveTo']);
            spyOn(mgr, 'nodeMenuPosition');
            const position = {x: 0, y: 0};
            mgr.nodeMenuPosition.and.returnValue(position);
            mgr.menus = {1: node};

            mgr.updatePositions();
            expect(node.moveTo).toHaveBeenCalledWith(position);
        });
    });
});
