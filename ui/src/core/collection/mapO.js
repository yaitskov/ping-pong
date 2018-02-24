export default function (nullableList, mapperF, fallback) {
    if (nullableList && nullableList.length) {
        return nullableList.map(mapperF);
    }
    return fallback;
}
