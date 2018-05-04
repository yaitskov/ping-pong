import angular from 'angular';
import TranslatingDropDown from './TranslatingDropDown.js';

angular.module('widget').
    /**
       <translating-drop-down ng-model="$ctrl.lang" domain="['en', 'ru', 'pl']"/>
    */
    directive('translatingDropDown', [() => new TranslatingDropDown()]);
