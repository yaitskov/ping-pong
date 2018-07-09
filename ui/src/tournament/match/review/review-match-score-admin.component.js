import angular from 'angular';
import template from './review-match-score-admin.template.html';
import ReviewMatchScoreCtrl from './ReviewMatchScoreCtrl.js';

angular.
    module('tournament').
    component('reviewMatchScoreForAdmin', {
        templateUrl: template,
        controller: ReviewMatchScoreCtrl});
