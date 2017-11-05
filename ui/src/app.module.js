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
    'signUp',
    'signIn',
    'doSignIn',
    'account',
    'accountEdit',
    'placeList',
    'placeDetail',
    'myPlace',
    'myPlaceEdit',
    'myTableList',
    'newPlace',
    'placePicker',
    'myTournamentList',
    'copyTournament',
    'newTournament',
    'tournamentEdit',
    'tournamentParameters',
    'tournament',
    'changeCategory',
    'categoryMemberList',
    'tournamentCategories'
]);
