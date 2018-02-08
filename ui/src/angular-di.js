export default function injectArgs(obj, constructorArgs) {
    const clazz = obj.constructor;
    const depNames = clazz.$inject;
    if (depNames.length != constructorArgs.length) {
        throw new Error(`Mismatch between dependencies names`
                        + ` and dependencies slots in class: [${clazz.name}]`);
    }
    for (let i = 0; i < depNames.length; ++i) {
        const dependency = constructorArgs[i];
        if (!dependency) {
            throw new Error(`Dependency [${depNames[i]}] of class [${clazz.name}] is [${dependency}]`);
        } else if (dependency.constructor && dependency.constructor.name != depNames[i]) {
            throw new Error(`Dependency [${depNames[i]}] of class [${clazz.name}] is [${dependency.constructor.name}]`);
        }

        obj[depNames[i]] = dependency;
    }
}
