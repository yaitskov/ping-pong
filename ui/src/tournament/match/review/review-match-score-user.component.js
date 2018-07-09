import angular from 'angular';
import template from './review-match-score-user.template.html';
import ReviewMatchScoreCtrl from './ReviewMatchScoreCtrl.js';

angular.
    module('tournament').
    component('reviewMatchScoreForUser', {
        templateUrl: template,
        controller: ReviewMatchScoreCtrl});
