import angular from 'angular';
import './enlist.scss';
import template from './enlist.offline.template.html';
import EnlistOfflineCtrl from './EnlistOfflineCtrl.js';

angular.
    module('participant').
    component('enlistOffline', {
        templateUrl: template,
        controller: EnlistOfflineCtrl});
