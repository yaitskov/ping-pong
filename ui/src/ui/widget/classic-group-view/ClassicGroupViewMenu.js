import SimpleController from 'core/angular/SimpleController.js';
import ClassicGroupViewCtrl from './ClassicGroupViewCtrl.js';

export default class ClassicGroupViewMenu extends SimpleController {
    changeSortBy(order) {
        this.send(ClassicGroupViewCtrl.TopicSetRowOrder, order);
    }

    $onInit() {
        this.groupHashExtraMatches = false;
        this.showExtraMatches = false;
        this.subscribe(ClassicGroupViewCtrl.TopicSetRowOrder,
                       (order) => this.sortBy = order);
        this.subscribe(ClassicGroupViewCtrl.TopicLoad,
                       (tournament) => this.findIfGroupHasExtraMatch(tournament));
        this.changeSortBy('final');
    }

    toggleShowExtraMatches() {
        this.showExtraMatches = this.showExtraMatches ^ true;
        this.send(ClassicGroupViewCtrl.TopicSetTagFilter,
                  this.showExtraMatches ? ClassicGroupViewCtrl.Dm : ClassicGroupViewCtrl.Origin);
    }

    findIfGroupHasExtraMatch(tournament) {
        this.groupHashExtraMatches = false;
        for (let p of tournament.participants) {
            if (p.dmMatches) {
                this.groupHashExtraMatches = true;
                break;
            }
        }
    }
}
