import angular from 'angular';
import template from './manage-one.template.html';
import ManageOneCtrl from './ManageOneCtrl.js';

angular.module('participant').
    component('manageOneParticipant', {
        templateUrl: template,
        controller: ManageOneCtrl});
