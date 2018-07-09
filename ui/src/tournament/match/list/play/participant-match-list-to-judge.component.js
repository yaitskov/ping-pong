import angular from 'angular';
import './participant-match-list-to-judge.scss';
import template from './participant-match-list-to-judge.template.html';
import ParticipantMatchListToJudgeCtrl from './ParticipantMatchListToJudgeCtrl.js';

angular.module('tournament').
    component('participantMatchListToJudge', {
        templateUrl: template,
        controller: ParticipantMatchListToJudgeCtrl});
