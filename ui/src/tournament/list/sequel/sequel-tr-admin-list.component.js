import angular from 'angular';
import '../../list.scss';
import template from './sequel-tr-list.template.html';
import SequelTournamentListCtrl from './SequelTournamentListCtrl.js';

class SequelTournamentAdminListCtrl extends SequelTournamentListCtrl {
    viewUrl(tournament) {
        return `/my/tournament/${tournament.tid}`;
    }
}

angular.module('tournament').
    component('sequelTournamentAdminList', {
        templateUrl: template,
        controller: SequelTournamentAdminListCtrl});
