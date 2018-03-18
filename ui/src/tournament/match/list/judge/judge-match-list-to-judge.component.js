import angular from 'angular';
import './judge-match-list.scss';
import JudgeMatchListCtrl from './JudgeMatchListCtrl.js';
import template from './judge-match-list-to-judge.template.html';

angular.module('tournament').
    component('judgeMatchListToJudge', {
        templateUrl: template,
        controller: JudgeMatchListCtrl
    });
