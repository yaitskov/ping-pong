import angular from 'angular';
import personNameField from './personNameField.js';

angular.module('widget').
    /**
       <person-name-field ng-model="$ctrl.participantName"/>
    */
    directive('personNameField', personNameField);
