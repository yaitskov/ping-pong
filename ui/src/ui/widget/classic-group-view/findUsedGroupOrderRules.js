export default function(reasonChainList) {
    const result = [];
    for (let reasonChain of reasonChainList) {
        for (let i = 0; i < reasonChain.length; ++i) {
            const reason = reasonChain[i];
            if (reason['@type'] != 'INF') {
                result[i] = reason.rule;
            }
        }
    }
    return result.filter(rule => rule);
}
