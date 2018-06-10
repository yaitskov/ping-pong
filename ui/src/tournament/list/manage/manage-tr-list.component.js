import angular from 'angular';
import '../../list.scss';
import template from './manage-tr-list.template.html';
import ManageTrListCtrl from './ManageTrListCtrl.js';

angular.module('tournament').
    component('manageTournamentList', {
        templateUrl: template,
        controller: ManageTrListCtrl});
