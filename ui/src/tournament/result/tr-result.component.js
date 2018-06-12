import angular from 'angular';
import 'css/medal.scss';
import '../tournament-result.scss';
import './tr-result.scss';
import template from './tr-result.template.html';
import TournamentResultCtrl from './TournamentResultCtrl.js';

angular.
    module('tournament').
    component('tournamentResult', {
        templateUrl: template,
        controller: TournamentResultCtrl});
