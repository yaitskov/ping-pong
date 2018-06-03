import angular from 'angular';
import injectableDirective from 'core/angular/injectableDirective.js';
import MainMenuService from './MainMenuService.js';
angular.
    module('mainMenu').
    factory('mainMenu', injectableDirective(MainMenuService));
