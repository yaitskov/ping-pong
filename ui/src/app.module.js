import angular from 'angular';
import angularTranslate from 'angular-translate';
import coreModule from './core/core.module.js';

angular.module('pingPong', [
    'angularMoment',
    'ngRoute',
    'nk.touchspin',
    'pascalprecht.translate',
    'ui',
    'core',
    'user',
    'place',
    'tournament'
]);
