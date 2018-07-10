import angular from 'angular';
import template from './tr-group-switcher.template.html';
import TrGroupSwitcherCtrl from './TrGroupSwitcherCtrl.js';

angular.module('participant').
    component('trGroupMemberSwitcher', {
        templateUrl: template,
        controller: TrGroupSwitcherCtrl});
