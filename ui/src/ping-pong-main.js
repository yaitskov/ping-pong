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

import signupModule from './sign-up/sign-up.module.js';
import signupComponent from './sign-up/sign-up.component.js';
import signinModule from './sign-in/sign-in.module.js';
import signinComponent from './sign-in/sign-in.component.js';
import signinModule from './sign-in/do/do-sign-in.module.js';
import signinComponent from './sign-in/do/do-sign-in.component.js';
import accountModule from './account/account.module.js';
import accountComponent from './account/account.component.js';
import accountEditModule from './account/edit.module.js';
import accountEditComponent from './account/edit.component.js';
import './ui/ui.import.js';
import placepickerModule from './place-picker/place-picker.module.js';
import placepickerService from './place-picker/place-picker.service.js';
import placepickerComponent from './place-picker/place-picker.component.js';
import placeListModule from './place-list/place-list.module.js';
import placeListComponent from './place-list/place-list.component.js';
import newPlaceModule from './new-place/new-place.module.js';
import newPlaceComponent from './new-place/new-place.component.js';
import copyTournamentModule from './new-tournament/copy-tournament.module.js';
import copyTournamentComponent from './new-tournament/copy-tournament.component.js';
import newTournamentModule from './new-tournament/new-tournament.module.js';
import newTournamentComponent from './new-tournament/new-tournament.component.js';

import myTournamentListModule from './my-tournament-list/my-tournament-list.module.js';
import myTournamentListComponent from './my-tournament-list/my-tournament-list.component.js';
import myTournamentCategoriesModule from './my-tournament/categories/categories.module.js';
import myTournamentCategoriesComponent from './my-tournament/categories/categories.component.js';
import myTournamentMembersModule from './my-tournament/categories/members/members.module.js';
import myTournamentMembersComponent from './my-tournament/categories/members/members.component.js';
import myTournamentChangeModule from './my-tournament/categories/change/change.module.js';
import myTournamentChangeComponent from './my-tournament/categories/change/change.component.js';
import myTournamentEditModule from './my-tournament/edit/edit.module.js';
import myTournamentEditComponent from './my-tournament/edit/edit.component.js';
import './tournament/tournament.import.js';
import myTournamentParametersModule from './my-tournament/parameters/parameters.module.js';
import myTournamentParametersComponent from './my-tournament/parameters/parameters.component.js';
import placeDetailModule from './place-detail/place-detail.module.js';
import placeDetailComponent from './place-detail/place-detail.component.js';
import myPlaceModule from './my-place/my-place.module.js';
import myPlaceComponent from './my-place/my-place.component.js';
import myPlaceEditModule from './my-place/edit.module.js';
import myPlaceEditComponent from './my-place/edit.component.js';
import myPlaceTableModule from './my-place/tables.module.js';
import myPlaceTableComponent from './my-place/tables.component.js';

require('offline-plugin/runtime').install();
