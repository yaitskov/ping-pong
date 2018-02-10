import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './group-tr-params.template.html';
import GroupParamsCtrl from './GroupParamsCtrl.js';

angular.
    module('tournament').
    component('groupTrParams', {
        templateUrl: template,
        require: {
            parent: '^^tournamentParametersForm'
        },
        controller: GroupParamsCtrl});
