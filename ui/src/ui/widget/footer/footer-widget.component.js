import angular from 'angular';
import template from './footer.template.html';
import './footer.scss';
import Footer from './Footer.js';

angular.
    module('widget').
    component('footer', {
        templateUrl: template,
        controller: Footer});
