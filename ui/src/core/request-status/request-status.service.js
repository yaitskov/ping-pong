import angular from 'angular';
import RequestStatusService from './RequestStatusService.js';
import injectableDirective from 'core/angular/injectableDirective.js';

angular.
    module('core.requestStatus').
    factory('requestStatus', injectableDirective(RequestStatusService));
