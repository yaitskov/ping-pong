import template from './participant-order.template.html';
import ClassicGroupViewMenu from './ClassicGroupViewMenu.js';

angular.
    module('widget').
    component('participantOrder', {
        templateUrl: template,
        controller: ClassicGroupViewMenu});
