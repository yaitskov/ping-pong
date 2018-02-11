export default function injectArgs(obj, constructorArgs) {
    const clazz = obj.constructor;
    const depNames = clazz.$inject;
    if (depNames.length != constructorArgs.length) {
        throw new Error(`Mismatch between dependencies names`
                        + ` and dependencies slots in class: [${clazz.name}] ${depNames.length} != ${constructorArgs.length}`);
    }
    for (let i = 0; i < depNames.length; ++i) {
        const dependency = constructorArgs[i];
        if (!dependency) {
            throw new Error(`Dependency [${depNames[i]}] of class [${clazz.name}] is [${dependency}]`);
        }
        obj[depNames[i]] = dependency;
    }
}
