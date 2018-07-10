import angular from 'angular';
import template from './confirm-participant-expel.template.html';
import ConfirmParticipantExpelCtrl from './ConfirmParticipantExpelCtrl.js';

angular.module('participant').
    component('confirmParticipantExpel', {
        templateUrl: template,
        controller: ConfirmParticipantExpelCtrl});
