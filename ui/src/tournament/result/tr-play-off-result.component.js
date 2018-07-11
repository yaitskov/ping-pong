import angular from 'angular';
import template from './tr-play-off-result.template.html';
import TrPlayOffResultCtrl from './TrPlayOffResultCtrl.js';

angular.
    module('tournament').
    component('tournamentPlayOffResult', {
        templateUrl: template,
        controller: TrPlayOffResultCtrl});
