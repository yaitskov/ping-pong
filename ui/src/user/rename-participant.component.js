import angular from 'angular';
import RenameParticipantCtrl from './RenameParticipantCtrl.js';
import template from './rename-participant.template.html';

angular.
    module('user').
    component('renameParticipant', {
        templateUrl: template,
        controller: RenameParticipantCtrl});


