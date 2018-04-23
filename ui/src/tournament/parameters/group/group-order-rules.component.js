import angular from 'angular';
import template from './group-order-rules.template.html';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';

angular.
    module('tournament').
    component('groupOrderRules', {
        templateUrl: template,
        controller: GroupOrderRulesCtrl});
