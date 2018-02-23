import defaultTournamentRules from 'tournament/new/defaultTournamentRules.js';

export function newTournament(sport) {
    return {rules: defaultTournamentRules(sport)};
}

export function existingTournament(sport) {
    const tournament = newTournament(sport);
    tournament.tid = 1;
    return tournament;
}

export function existingTournamentWithoutGroup(sport) {
    const tournament = existingTournament(sport);
    delete tournament.rules.group;
    return tournament;
}

export function existingTournamentWithGroup(sport) {
    return existingTournament(sport);
}

export function newTournamentWithoutGroup(sport) {
    const tournament = newTournament(sport);
    delete tournament.rules.group;
    return tournament;
}

export function newTournamentWithGroup(sport) {
    return newTournament(sport);
}

export function existingTournamentWithoutPlayOff(sport) {
    const tournament = existingTournament(sport);
    delete tournament.rules.playOff;
    return tournament;
}

export function newTournamentWithoutPlayOff(sport) {
    const tournament = newTournament(sport);
    delete tournament.rules.playOff;
    return tournament;
}

export function newTournamentWithPlayOff(sport) {
    return newTournament(sport);
}

export function existingTournamentWithoutConsole(sport) {
    const tournament = existingTournament(sport);
    tournament.rules.group.console = 'NO';
    return tournament;
}

export function existingTournamentRequiresConsole(sport) {
    const tournament = existingTournament(sport);
    tournament.rules.group.console = 'INDEPENDENT_RULES';
    return tournament;
}

export function existingTournamentWithConsole(sport) {
    const tournament = existingTournamentRequiresConsole(sport);
    tournament.consoleTid = 2;
    return tournament;
}

export function existingLayeredConsoleTournament(sport) {
    const tournament = existingTournament(sport);
    tournament.masterTid = 2;
    tournament.rules.casting.policy = 'MasterOutcome';
    tournament.rules.casting.splitPolicy = 'ConsoleLayered';
    return tournament;
}
