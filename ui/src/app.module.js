import angular from 'angular';
import angularTranslate from 'angular-translate';
import coreModule from './core/core.module.js';

angular.module('pingPong', [
    'ngRoute',
    'nk.touchspin',
    'pascalprecht.translate',
    'mainMenu',
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
    'playInTournamentList',
    'copyTournament',
    'newTournament',
    'tournamentEdit',
    'enlistOffline',
    'tournamentParameters',
    'newTournamentParameters',
    'myTournament',
    'matchStateDescription',
    'completeMyMatch',
    'changeCategory',
    'adminScoreSet',
    'matchScoreConflict',
    'reviewMatchScore',
    'participant',
    'participantPresence',
    'categoryMemberList',
    'tournamentCategories',
    'tournamentDetail',
    'myMatchPlayList',
    'myMatchJudgeList',
    'openTournamentList',
    'tournamentResult',
    'openTournament',
    'tournamentList'
]);
