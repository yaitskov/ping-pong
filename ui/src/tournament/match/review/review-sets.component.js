import angular from 'angular';
import template from './review-sets.template.html';
import ReviewSetsCtrl from './ReviewSetsCtrl.js';

angular.
    module('tournament').
    component('reviewSets', {
        templateUrl: template,
        controller: ReviewSetsCtrl});
