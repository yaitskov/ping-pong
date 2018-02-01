import './vis-menu.css';

export default class NodeMenuManager {
    updatePositions() {
        for (let [nodeId, mn] of Object.entries(this.menus)) {
            mn.moveTo(this.nodeMenuPosition(nodeId));
        }
    }

    removeMenu(nodeId) {
        let menu = this.menus[nodeId];
        delete this.menus[nodeId];
        menu.remove();
    }

    removeMenus(nodeIds) {
        for (let [nodeId, menu] of Object.entries(this.menus)) {
            if (nodeIds.indexOf(nodeId) < 0) {
                this.removeMenu(nodeId);
            }
        }
    }

    nodeMenuPosition(nodeId) {
        let bBox = this.visNetwork.getBoundingBox(nodeId);
        return this.visNetwork.canvasToDOM({x: bBox.right + this.hSpace, y: bBox.top});
    }

    addMenu(nodeId) {
        let menu = this.nodeMenuFactory.create(nodeId);
        this.menus[nodeId] = menu;
        menu.attach(this.visContainer);
        return menu;
    }

    findWidestNode() {
        let nodeIds = this.nodes.getIds();
        if (!nodeIds.length) {
            return 0;
        }
        return Math.max.apply(
            null,
            nodeIds.map((nodeId) => {
                let box = this.visNetwork.getBoundingBox(nodeId);
                return box.right - box.left;
            }));
    }

    recalculateLayerDistance() {
        if (this.validLayerDistance) {
            return;
        }
        let newLayerDistance = this.findWidestNode() * this.widestNodeK;
        if (this.layerDistance == newLayerDistance) {
            return;
        }
        this.validLayerDistance = true;
        this.layerDistance = newLayerDistance;
        this.visOptions.layout.hierarchical.levelSeparation = newLayerDistance;
        this.visNetwork.setOptions(this.visOptions);
    }

    addMenus() {
        for (let nodeId of this.visNetwork.getSelectedNodes()) {
            if (this.menus[nodeId]) {
                continue;
            }
            this.addMenu(nodeId).moveTo(this.nodeMenuPosition(nodeId));
        }
    }

    bindVisNetwork() {
        this.visNetwork.on('dragging', () => this.updatePositions());
        this.visNetwork.on('zoom', () => this.updatePositions());
        this.visNetwork.on('stabilized', () => this.updatePositions());
        this.visNetwork.on('click', () => this.addMenus());
        this.visNetwork.on('afterDrawing', () => this.recalculateLayerDistance());
        this.visNetwork.on('deselectNode', (e) => this.removeMenus(e.nodes));
    }

    constructor(visContainer, visNetwork, visOptions, nodes, nodeMenuFactory) {
        this.visContainer = visContainer;
        this.visNetwork = visNetwork;
        this.visOptions = visOptions;
        this.nodes = nodes;
        this.nodeMenuFactory = nodeMenuFactory;
        this.menus = {};
        this.hSpace = 10;
        this.widestNodeK = 1.3;

        this.bindVisNetwork();
    }
}
