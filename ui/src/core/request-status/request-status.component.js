import './request-status.scss';
import angular from 'angular';
import template from './request-status.template.html';
import RequestStatusComponent from './RequestStatusComponent.js';

angular.
    module('core.requestStatus').
    component('requestStatus', {
        templateUrl: template,
        controller: RequestStatusComponent});
