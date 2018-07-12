import angular from 'angular';
import template from './participant-score-set.template.html';
import ParticipantScoreSetCtrl from './ParticipantScoreSetCtrl.js';

angular.
    module('tournament').
    component('participantScoreSet', {
        templateUrl: template,
        controller: ParticipantScoreSetCtrl});
