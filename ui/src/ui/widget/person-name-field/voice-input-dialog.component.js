import angular from 'angular';
import template from './voice-input-dialog.template.html';
import VoiceInputDialog from './VoiceInputDialog.js';

angular.
    module('widget').
    component('voiceInputDialog', {
        templateUrl: template,
        controller: VoiceInputDialog});