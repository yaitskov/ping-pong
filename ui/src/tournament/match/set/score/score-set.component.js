import angular from 'angular';
import template from './score-set.template.html';
import ScoreSetCtrl from './ScoreSetCtrl.js';

angular.
    module('scoreSet').
    component('scoreSet', {
        templateUrl: template,
        controller: ScoreSetCtrl});
