import angular from 'angular';
import jQuery from 'jquery';
import translateTables from './translate.js';

angular.module('pingPong').
    config(['$httpProvider', function($httpProvider) {
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }
        $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
        $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
        $httpProvider.interceptors.push('injectingSessionInterceptor');
    }]).
    run(['$rootScope', function ($rootScope) {
        $rootScope.$on('$locationChangeStart', function (event) {
            jQuery(".modal-backdrop").remove();
        });
    }]).
    service('injectingSessionInterceptor', ['auth', function (auth) {
        var ser = this;
        ser.request = function (config) {
            var session = auth.mySession();
            if (session) {
                if (config.headers.session) {
                    config.headers.session = session;
                }
            } else if (config.headers.session) {
                // prevent sending request
                console.log("Error session is missing");
            }
            return config;
        };
    }]).
    config(['$locationProvider', '$routeProvider', '$translateProvider',
            function config($locationProvider, $routeProvider, $translateProvider) {
                $translateProvider
                    .translations('pl', translateTables.pl)
                    .preferredLanguage('pl');
                $locationProvider.hashPrefix('!');
                $routeProvider.
                    when('/tournaments', {
                        template: '<tournament-list></tournament-list>'
                    }).
                    when('/watch/tournaments', {
                        template: '<open-tournament-list></open-tournament-list>'
                    }).
                    when('/play/in/tournaments', {
                        template: '<play-in-tournament-list></play-in-tournament-list>'
                    }).
                    when('/complete/my/match/:matchId', {
                        template: '<complete-my-match></complete-my-match>'
                    }).
                    when('/complete/match/:matchId', {
                        template: '<complete-match></complete-match>'
                    }).
                    when('/my/matches/play', {
                        template: '<my-match-play-list></my-match-play-list>'
                    }).
                    when('/match/state/description', {
                        template: '<match-state-description></match-state-description>'
                    }).
                    when('/my/tournament/:tournamentId/category/:categoryId/members', {
                        template: '<category-member-list></category-member-list>'
                    }).
                    when('/my/tournament/edit/:tournamentId', {
                        template: '<tournament-edit></tournament-edit>'
                    }).
                    when('/my/tournament/parameters/:tournamentId', {
                        template: '<tournament-parameters></tournament-parameters>'
                    }).
                    when('/my/tournament/:tournamentId/change-category/:participantId', {
                        template: '<change-category></change-category>'
                    }).
                    when('/my/matches/judgement', {
                        template: '<my-match-judge-list></my-match-judge-list>'
                    }).
                    when('/watch/tournament/:tournamentId', {
                        template: '<open-tournament></open-tournament>'
                    }).
                    when('/tournament/new', {
                        template: '<new-tournament></new-tournament>'
                    }).
                    when('/tournament/new/parameters', {
                        template: '<new-tournament-parameters></new-tournament-parameters>'
                    }).
                    when('/my/new/place', {
                        template: '<new-place></new-place>'
                    }).
                    when('/place/:placeId', {
                        template: '<place-detail></place-detail>'
                    }).
                    when('/my-place/:placeId', {
                        template: '<my-place></my-place>'
                    }).
                    when('/my-place/edit/:placeId', {
                        template: '<my-place-edit></my-place-edit>'
                    }).
                    when('/my-place/tables/:placeId', {
                        template: '<my-table-list></my-table-list>'
                    }).
                    when('/pick/place', {
                        template: '<place-picker></place-picker>'
                    }).
                    when('/my/tournament/presence/:tournamentId', {
                        template: '<participant-presence></participant-presence>'
                    }).
                    when('/my/tournament/:tournamentId/participant/:userId', {
                        template: '<participant></participant>'
                    }).
                    when('/my/tournament/categories/:tournamentId', {
                        template: '<tournament-categories></tournament-categories>'
                    }).
                    when('/my/places', {
                        template: '<place-list></place-list>'
                    }).
                    when('/my/tournaments', {
                        template: '<my-tournament-list></my-tournament-list>'
                    }).
                    when('/sign-up', {
                        template: '<sign-up></sign-up>'
                    }).
                    when('/sign-in', {
                        template: '<sign-in></sign-in>'
                    }).
                    when('/do-sign-in/:oneTimeSignInToken/:email', {
                        template: '<do-sign-in></do-sign-in>'
                    }).
                    when('/account', {
                        template: '<account></account>'
                    }).
                    when('/account/profile/edit', {
                        template: '<account-edit></account-edit>'
                    }).
                    when('/tournaments/:tournamentId', {
                        template: '<tournament-detail></tournament-detail>'
                    }).
                    when('/tournament/result/:tournamentId', {
                        template: '<tournament-result></tournament-result>'
                    }).
                    when('/my/tournament/:tournamentId', {
                        template: '<my-tournament></my-tournament>'
                    }).
                    when('/tournament/enlist/offline/:tournamentId', {
                        template: '<enlist-offline></enlist-offline>'
                    }).
                    otherwise('/tournaments');
            }
           ]);
