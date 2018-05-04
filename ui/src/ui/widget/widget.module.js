import angular from 'angular';
import VoiceInput from './person-name-field/VoiceInput.js';

angular.module('widget', ['core.util']).
   service('VoiceInput', VoiceInput);

