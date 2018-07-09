import angular from 'angular';
import '../../list.scss';
import './watch-tournament-list.scss';
import '../../open-tournament.scss';
import template from './watch-tournament-list.template.html';
import TournamentListToWatchCtrl from './TournamentListToWatchCtrl.js';

angular.module('tournament').
    component('watchTournamentList', {
        templateUrl: template,
        controller: TournamentListToWatchCtrl});
