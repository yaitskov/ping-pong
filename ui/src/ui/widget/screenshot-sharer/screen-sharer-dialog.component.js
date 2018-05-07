import angular from 'angular';
import template from './screen-sharer-dialog.template.html';
import ScreenSharerDialog from './ScreenSharerDialog.js';

angular.
    module('widget').
    component('screenSharerDialog', {
        templateUrl: template,
        controller: ScreenSharerDialog});