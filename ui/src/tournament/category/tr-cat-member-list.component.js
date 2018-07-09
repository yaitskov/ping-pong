import angular from 'angular';
import template from './tr-cat-member-list.template.html';
import TrCategoryMemberListCtrl from './TrCategoryMemberListCtrl.js';

angular.module('tournamentCategory').
    component('trCategoryMemberList', {
        templateUrl: template,
        controller: TrCategoryMemberListCtrl});
