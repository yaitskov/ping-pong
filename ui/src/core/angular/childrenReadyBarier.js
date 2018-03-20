export default function (ctrl) {
    const eventNames = ctrl.expectedChildCtrls.map(cls => cls.readyEvent);
    const readyChildCtrls = ctrl.eBarier.create(
        eventNames, () => ctrl.broadcast(ctrl.constructor.readyEvent));
    const eventHandlers = {};
    for (let event of eventNames) {
        eventHandlers[event] = (e) => readyChildCtrls.got(event);
    }
    ctrl.$bind(eventHandlers);
}
