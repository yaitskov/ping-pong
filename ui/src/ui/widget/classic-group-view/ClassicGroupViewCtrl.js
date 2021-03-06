import SimpleController from 'core/angular/SimpleController.js';
import findRowSpan from './findRowSpan.js';
import findUsedGroupOrderRules from './findUsedGroupOrderRules.js';

export default class ClassicGroupViewCtrl extends SimpleController {
    static get TopicLoad() {
        return 'classic-group-view-load';
    }

    static get TopicSetRowOrder() {
        return 'classic-group-view-set-row-order';
    }

    static get TopicSetShowMode() {
        return 'classic-group-view-set-show-mode';
    }

    static get TopicSetTagFilter() {
        return 'classic-group-view-set-tag-filter';
    }

    static get Dm() {
        return 'dm';
    }

    static get $inject() {
        return ['InfoPopup'].concat(super.$inject);
    }

    static get Origin() {
        return 'origin';
    }

    $onInit() {
        this.subscribe(this.constructor.TopicLoad, (gr) => this.loadData(gr));
        this.subscribe(this.constructor.TopicSetRowOrder, (order) => this.setRowOrder(order));
        this.subscribe(this.constructor.TopicSetShowMode, (mode) => this.setScoreShowMode(mode));
        this.subscribe(this.constructor.TopicSetTagFilter, (tag) => this.setTagFilter(tag));

        this.sorters = {
            'seed':  (a, b) => a.seedPosition - b.seedPosition,
            'final': (a, b) => a.finishPosition - b.finishPosition,
            'name':   (a, b) => a.name.localeCompare(b.name)
        };
        this.scoreShowMode = 'sets';
        this.rowOrder = 'final'; // final | abc
        this.matchTagFilter = this.constructor.Origin; // origin | dm
        this.usedGroupOrderRules = [];
        this.rowSpan = {};
    }

    showHelpForRule(ruleId) {
        this.InfoPopup.transInfo(`group-order-rule-${ruleId}-description`);
    }

    loadData(tournament) {
        this.participants = tournament.participants;
        this._selectMatchesByTag();
        this.tournamentId = tournament.tid;
        this.quitsGroup = tournament.quitsGroup;
        this.sportType = tournament.sportType;
        this.setRowOrder('final');
    }

    _selectMatchesByTag() {
        for (let participant of this.participants) {
            switch (this.matchTagFilter) {
            case this.constructor.Origin:
                participant.matches = participant.originMatches;
                break;
            case this.constructor.Dm:
                participant.matches = participant.dmMatches || {};
                break;
            default:
                throw new Error(`unknown matchTagFilter "${this.matchTagFilter}"`);
            }
        }
    }

    setTagFilter(tag) {
        this.matchTagFilter = tag;
        this._selectMatchesByTag();
    }

    setRowOrder(order) {
        this.rowOrder = order;
        this._update();
    }

    _update() {
        if (this.participants) {
            this.participants.sort(this.sorters[this.rowOrder]);
            const reasonChainList = this.participants.map((p) => p.reasonChain);
            this.rowSpan = findRowSpan(reasonChainList, (reason) => reason['@type'] == 'INF');
            this.usedGroupOrderRules =  findUsedGroupOrderRules(reasonChainList);
        }
    }

    setScoreShowMode(mode) {
        this.scoreShowMode = mode;
    }

    isLost(p1, p2) {
        if (!p1.matches) {
            return false;
        }
        var m = p1.matches[p2.bid];
        if (m) {
            return m.sets.his < m.sets.enemy;
        }
        return false;
    }

    isWon(p1, p2) {
        if (!p1.matches) {
            return false;
        }
        var m = p1.matches[p2.bid];
        if (m) {
            return m.sets.his > m.sets.enemy;
        }
        return false;
    }
}
