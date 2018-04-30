export default function (reason, reasonAbove) {
    return reason.value == reasonAbove.value && reason['@type'] == reasonAbove['@type'];
}
