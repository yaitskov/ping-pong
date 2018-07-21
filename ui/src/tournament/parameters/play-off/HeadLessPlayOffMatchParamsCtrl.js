import HeadLessMatchParamsCtrl from 'tournament/parameters/match/HeadLessMatchParamsCtrl.js';

export default class HeadLessPlayOffMatchParamsCtrl extends HeadLessMatchParamsCtrl {
    static get TopicLoad() {
        return 'head-less-play-off-match-params-load';
    }
}