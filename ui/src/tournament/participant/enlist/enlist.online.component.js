import angular from 'angular';
import './enlist.scss';
import template from './enlist.online.template.html';
import EnlistOnlineCtrl from './EnlistOnlineCtrl.js';

angular.
    module('participant').
    component('enlistOnline', {
        templateUrl: template,
        controller: EnlistOnlineCtrl});
