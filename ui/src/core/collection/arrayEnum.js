export default function arrayEnum() {
    const result = {};
    for (let i = 0; i < arguments.length; ++i) {
        result[arguments[i]] = arguments[i];
    }
    return result;
}
