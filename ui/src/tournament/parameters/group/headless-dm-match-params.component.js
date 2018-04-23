import angular from 'angular';
import template from '../match/headless-match-params.template.html';
import HeadlessDmMatchParamsCtrl from './HeadlessDmMatchParamsCtrl.js';

angular.
    module('tournament').
    component('headlessDmMatchParams', {
        templateUrl: template,
        controller: HeadlessDmMatchParamsCtrl});
