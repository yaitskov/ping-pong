import SimpleController from 'core/angular/SimpleController.js';

export default class HeadLessMatchParamsCtrl extends SimpleController {
    static get TopicLoad() {
        return 'head-less-match-params-load';
    }

    // constructor(...params) {
    //     super(...params);
    //     this.advance = {min: 1, max: 1000};
    //     this.score = {min: 1, max: 1000};
    //     this.sets = {min: 1, max: 1000};
    // }

    $onInit() {
        this.advance = {min: 1, max: 1000};
        this.score = {min: 1, max: 1000};
        this.sets = {min: 1, max: 1000};

        this.subscribe(this.constructor.TopicLoad, (rules) => this.rules = rules);
    }
}
