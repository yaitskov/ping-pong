import angular from 'angular';
import './main-menu.scss';
import template from './main-menu.template.html';
import MainMenuComponent from './MainMenuComponent.js';

angular.
    module('mainMenu').
    component('mainMenu', {
        templateUrl: template,
        controller: MainMenuComponent});
