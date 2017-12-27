export default function (tournament, match) {
    let uids = Object.keys(match.score || {});
    if (match.state == 'Over') {
        let result = [];
        if (match.walkOver) {
            for (let i of 2) {
                let name = (uids[i] == 1) ? 'bye' : tournament.participants[uids[i]];
                result.push((uids[i] == match.winnerId ? ' ☺' : " ⚐") +  " - " + name);
            }
        } else {
            for (let i of 2) {
                let name = (uids[i] == 1) ? 'bye' : tournament.participants[uids[i]];
                result.push(String(match.score[uids[i]]).padStart(2, ' ')
                            + (uids[i] == match.winnerId ? ' + ' : " - ") + name);
            }
        }
        return result.join("\n");
    } else if (match.state == 'Draft') {
        if (uids.length == 1) {
            return " ⌛ - " + tournament.participants[uids[0]] + "\n ⌛ - ???";
        } else {
            return " ??? ";
        }
    } else if (match.state == 'Place') {
        let result = [];
        for (let i of 2) {
            let name = tournament.participants[uids[i]];
            result.push(" ⌛ - " + name);
        }
        return result.join("\n");
    } else if (match.state == 'Game') {
        let result = [];
        for (let i of 2) {
            let name = tournament.participants[uids[i]];
            result.push(String(match.score[uids[i]]).padStart(2, ' ') + " - " + name);
        }
        return result.join("\n");
    } else if (match.state == 'Auto') {
        return " ⚐ - " + tournament.participants[uids[0]] + "\n ☺ - ???";
    } else {
        return 'state: ' + match.state;
    }
};
