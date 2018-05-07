export default function (clazz) {
    return clazz.$inject.concat((...args) => new clazz(...args));
}