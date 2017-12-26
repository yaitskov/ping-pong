export default class NodeMenu {
    constructor(nodeId, dom) {
        this.nodeId = nodeId;
        this.dom = dom;
    }

    onAttach() {
    }

    attach(domContainer) {
        this.onAttach();
        domContainer.append(this.dom);
    }

    remove() {
        this.dom.remove();
    }

    moveTo(point) {
        this.dom.css({left: point.x, top: point.y})
    }
}
