import defaultReasonEq from './reasonEq.js';

export default function (reasonChainList, voidP, reasonEq = defaultReasonEq) {
    const rowSpan = {};
    let previousReasonChain = null;
    let prevSpan = null;
    for (let iRow = 0; iRow < reasonChainList.length; ++iRow) {
        const reasonChain = reasonChainList[iRow];
        const span = rowSpan[iRow] = {};
        for (let iCol = 0; iCol < reasonChain.length; ++iCol) {
            const maxPrevCol = previousReasonChain === null ? -1 : previousReasonChain.length;
            if (maxPrevCol < iCol || !reasonEq(reasonChain[iCol], previousReasonChain[iCol])) {
                if (!voidP(reasonChain[iCol])) {
                    span[iCol] = 1;
                }
            } else if (prevSpan[iCol]) {
                prevSpan[iCol] += 1;
            } else {
                for (let r = iRow - 1; r >= 0; --r) {
                    if (rowSpan[r][iCol]) {
                        rowSpan[r][iCol] += 1;
                        break;
                    }
                }
            }
        }
        previousReasonChain = reasonChain;
        prevSpan = span;
    }
    return rowSpan;
}
