import injectArgs from 'angular-di.js';

export default class TournamentRulesCtrl {
    static get $inject() {
        return ['$scope', '$rootScope', 'binder', 'eBarier'];
    }

    get isValid() {
        for (let section of this.sections) {
            if (!section.isValid) {
                return false;
            }
        }
        return true;
    }

    update() {
        this.form.$setSubmitted();
        if (this.form.$valid && this.isValid) {
            this.broadcast('event.tournament.rules.update', this.tournament.rules);
        }
    }

    constructor() {
        injectArgs(this, arguments);
        this.sections = [];

        const ready = this.eBarier.create(['console', 'seeding', 'match',
                                           'group', 'play-off', 'arena'],
                                          () => this.broadcast('event.tournament.rules.ready'));
        this.binder(this.$scope, {
            'event.tournament.rules.errors': (e, errors) => this.errors = errors,
            'event.tournament.rules.set': (e, tournament) => this.tournament = tournament,
            'event.tournament.rules.seeding.ready': (e) => ready.got('seeding'),
            'event.tournament.rules.match.ready': (e) => ready.got('match'),
            'event.tournament.rules.group.ready': (e) => ready.got('group'),
            'event.tournament.rules.arena.ready': (e) => ready.got('arena'),
            'event.tournament.rules.play-off.ready': (e) => ready.got('play-off'),
            'event.tournament.rules.console.ready': (e) => ready.got('console')
        });
    }

    registerSection(sectionCtrl) {
        console.log(`register ctrl ${sectionCtrl.constructor.name}`);
        this.sections.push(sectionCtrl);
    }

    back() {
        this.broadcast('event.tournament.rules.back', this.rules);
    }

    cancel() {
        this.broadcast('event.tournament.rules.cancel', this.rules);
    }

    broadcast(topic, data) {
        this.$rootScope.$broadcast(topic, data);
    }
}
