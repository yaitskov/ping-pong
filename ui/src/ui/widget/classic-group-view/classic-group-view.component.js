import angular from 'angular';
import './classic-group-view.scss';
import ClassicGroupViewCtrl from './ClassicGroupViewCtrl.js';
import template from './classic-group-view.template.html';

angular.
    module('widget').
    component('classicGroupView', {
        templateUrl: template,
        controller: ClassicGroupViewCtrl});
