import angular from 'angular';
import template from './info-popup.template.html';
import InfoPopupWidget from './InfoPopupWidget.js';
import InfoPopupService from './InfoPopupService.js';

angular.
    module('widget').
    service('InfoPopup', InfoPopupService).
    component('infoPopupWidget', {
        templateUrl: template,
        controller: InfoPopupWidget});