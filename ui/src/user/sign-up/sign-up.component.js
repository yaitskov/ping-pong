import angular from 'angular';
import template from './sign-up.template.html';
import SignUpCtrl from './SignUpCtrl.js';

angular.
    module('user').
    component('signUp', {
        templateUrl: template,
        controller: SignUpCtrl});
