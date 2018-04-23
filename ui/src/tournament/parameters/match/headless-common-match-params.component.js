import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './headless-match-params.template.html';
import HeadLessCommonMatchParamsCtrl from './HeadLessCommonMatchParamsCtrl.js';

angular.
    module('tournament').
    component('headlessCommonMatchParams', {
        templateUrl: template,
        controller: HeadLessCommonMatchParamsCtrl});
