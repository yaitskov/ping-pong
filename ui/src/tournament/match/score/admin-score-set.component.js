import angular from 'angular';
import template from './admin-score-set.template.html';
import AdminScoreSetCtrl from './AdminScoreSetCtrl.js';

angular.
    module('tournament').
    component('adminScoreSet', {
        templateUrl: template,
        controller: AdminScoreSetCtrl});
