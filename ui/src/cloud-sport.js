document.getElementById("body").addEventListener("noupdate", function() {
    console.log("My NoUpdate hook");
    //document.getElementById("demo").innerHTML = "Hello World";
});

import 'flatpickr/dist/flatpickr.css';
import 'bootstrap/dist/css/bootstrap.css';
import 'jquery-ui-dist/jquery-ui.css';
import 'clockpicker/dist/bootstrap-clockpicker.css';
import 'bootstrap-toggle/css/bootstrap-toggle.css';
import 'angular-touchspin/dist/angular-touchspin.css';

import './css/app.scss';

import jQuery from 'jquery';
import 'jquery-ui-dist/jquery-ui.js';
import 'jquery-ui-touch-punch/jquery.ui.touch-punch.js';
import moment from 'moment/moment.js';
import flatpickr from 'flatpickr';
import angular from 'angular';
import angularRoute from 'angular-route';
import angularResource from 'angular-resource';
import angularMoment from 'angular-moment/angular-moment.js';
import angularTouchSpin from 'angular-touchspin';
import uiSortable from 'angular-ui-sortable/src/sortable.js';
import bootstrap from 'bootstrap/dist/js/bootstrap.js';
import bootstrapClockPicker from 'clockpicker/dist/bootstrap-clockpicker.js';
import 'bootstrap-toggle/js/bootstrap-toggle.js';

import appModule from './app.module.js';
import appConfig from './app.config.js';

import authModule from './auth/auth.module.js';
import authService from './auth/auth.service.js';

import './user/user.import.js';
import './ui/ui.import.js';
import './place/place.import.js';
import './tournament/tournament.import.js';

require('offline-plugin/runtime').install();
