import angular from 'angular';
import '../../list.scss';
import template from './sequel-tr-list.template.html';
import SequelTournamentListCtrl from './SequelTournamentListCtrl.js';

class SequelTournamentParticipantListCtrl extends SequelTournamentListCtrl {
    viewUrl(tournament) {
        return `/tournaments/${tournament.tid}`;
    }
}

angular.module('tournament').
    component('sequelTournamentParticipantList', {
        templateUrl: template,
        controller: SequelTournamentParticipantListCtrl});
