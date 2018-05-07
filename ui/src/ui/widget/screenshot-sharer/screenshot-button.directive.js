import angular from 'angular';
import ScreenshotButton from './ScreenshotButton.js';
import injectableDirective from 'core/angular/injectableDirective.js';

angular.module('widget').
    directive('screenshotButton', injectableDirective(ScreenshotButton));
