import angular from 'angular';
import 'css/toggle-btn.scss';
import template from 'tournament/parameters/match/headless-match-params.template.html';
import HeadLessPlayOffMatchParamsCtrl from './HeadLessPlayOffMatchParamsCtrl.js';

angular.
    module('tournament').
    component('headLessPlayOffMatchParams', {
        templateUrl: template,
        controller: HeadLessPlayOffMatchParamsCtrl});