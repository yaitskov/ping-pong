import AbstractAjaxInfo from './AbstractAjaxInfo.js';

export default class ScopedAjaxInfo extends AbstractAjaxInfo {
    constructor(scopedInfo, ...args) {
        super(...args);
        this.scope = scopedInfo;
    }

    bind($scope) {
        $scope.$on('$destroy', () => this.clear());
    }
}