import angular from 'angular';
import './tr-category-list.scss';
import template from './tr-category-list.template.html';
import TrCategoryListCtrl from './TrCategoryListCtrl.js';

angular.module('tournamentCategory').
    component('tournamentCategoryList', {
        templateUrl: template,
        controller: TrCategoryListCtrl});
