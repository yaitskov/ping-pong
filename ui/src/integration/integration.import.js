import angular from 'angular';
import Facebook from './fb/Facebook.js';

angular.module('integration', ['ng']).
    service('Facebook', Facebook);
