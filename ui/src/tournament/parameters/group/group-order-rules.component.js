import angular from 'angular';
import template from './group-order-rules.template.html';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';

angular.
    module('tournament').
    component('group-order-rules', {
        templateUrl: template,
        controller: GroupOrderRulesCtrl});
