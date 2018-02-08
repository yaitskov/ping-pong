export default function injectArgs(obj, constructorArgs) {
    const clazz = obj.constructor;
    const depNames = clazz.$inject;
    if (depNames.length != constructorArgs.length) {
        throw new Error(`Mismatch between dependencies names`
                        + ` and dependencies slots in class: [${clazz.name}]`);
    }
    for (let i = 0; i < depNames.length; ++i) {
        obj[depNames[i]] = constructorArgs[i];
    }
}
