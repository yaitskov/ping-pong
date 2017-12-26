// Object.prototype[Symbol.iterator] = function* () {
//     for (let key of Object.keys(this)) {
//         yield([ key, this[key] ])
//     }
// };

Object.prototype[Symbol.iterator] = function () {
    var self = this;
    var values = Object.keys(this);
    var i = 0;
    return {
        next: function () {
            return {
                value: [values[i], self[values[i++]]],
                done: i > values.length
            }
        }
    }
};
