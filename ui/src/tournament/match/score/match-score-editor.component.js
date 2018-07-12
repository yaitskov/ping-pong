import angular from 'angular';
import '../admin-score.scss';
import template from './match-score-editor.template.html';
import MatchScoreEditorCtrl from './MatchScoreEditorCtrl.js';

angular.
    module('tournament').
    component('matchScoreEditor', {
        templateUrl: template,
        controller: MatchScoreEditorCtrl});
