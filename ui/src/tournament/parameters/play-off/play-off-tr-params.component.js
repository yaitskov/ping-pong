import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './play-off-tr-params.template.html';
import PlayOffParamsCtrl from './PlayOffParamsCtrl.js';

angular.
    module('tournament').
    component('playOffTrParams', {
        templateUrl: template,
        require: {
            parent: '^^tournamentParametersForm'
        },
        controller: PlayOffParamsCtrl});
