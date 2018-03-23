import angular from 'angular';
import template from './base-score-set.template.html';
import BaseScoreSet from './BaseScoreSet.js';

angular.
    module('scoreSet').
    component('baseScoreSet', {
        templateUrl: template,
        controller: BaseScoreSet
    });
