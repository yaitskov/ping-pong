import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './arena-params.template.html';
import ArenaParamsCtrl from './ArenaParamsCtrl.js';

angular.
    module('tournament').
    component('arenaParams', {
        templateUrl: template,
        require: {
            parent: '^^tournamentParametersForm'
        },
        controller: ArenaParamsCtrl});
