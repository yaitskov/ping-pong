import angular from 'angular';
import PersonNameField from './PersonNameField.js';

angular.module('widget').
    /**
       <person-name-field ng-model="$ctrl.participantName"/>
    */
    directive('personNameField',
              ['MessageBus', (MessageBus) => new PersonNameField(MessageBus)]);
