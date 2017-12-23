module.exports = {
    padLeft: function (str, fill, targetLength) {
        var needToFill = targetLength - str.length;
        if (needToFill > 0) {
            for (var i = 0; i < needToFill; ++i) {
                str = fill + str;
            }
        }
        return str;
    }
};
