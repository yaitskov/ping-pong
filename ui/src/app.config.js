import angular from 'angular';
import jQuery from 'jquery';
var translateTables = require('./translate/translate.js');

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
                    // .useSanitizeValueStrategy(null)
                    .translations('en', translateTables.en)
                    .translations('pl', translateTables.pl)
                    .preferredLanguage(localStorage.getItem('myLang') || 'pl');
                $locationProvider.hashPrefix('!');
                $routeProvider.
                    when('/tournament/draft/list', {
                        template: '<tournament-draft-list/>'
                    }).
                    when('/watch/tournaments', {
                        template: '<watch-tournament-list/>'
                    }).
                    when('/play/in/tournaments', {
                        template: '<play-in-tournament-list/>'
                    }).
                    when('/participant/score/set/:matchId', {
                        template: '<participant-score-set/>'
                    }).
                    when('/judge/score/set/:tournamentId/:matchId', {
                        template: '<admin-score-set/>'
                    }).
                    when('/match/admin-conflict-review/:tournamentId/:matchId', {
                        template: '<match-score-conflict-admin/>'
                    }).
                    when('/tournament/rules/:tournamentId', {
                        template: '<tournament-rules/>'
                    }).
                    when('/tournament/played-matches/:tournamentId', {
                        template: '<participant-played-matches/>'
                    }).
                    when('/match/user-conflict-review/:tournamentId/:matchId', {
                        template: '<match-score-conflict-user/>'
                    }).
                    when('/review/admin-scored-match/:tournamentId/:matchId', {
                        template: '<review-match-score-for-admin/>'
                    }).
                    when('/match/management/:tournamentId/:matchId', {
                        template: '<match-management/>'
                    }).
                    when('/tournament/enlisted/:tournamentId', {
                        template: '<enlisted-participants/>'
                    }).
                    when('/review/user-scored-match/:tournamentId/:matchId', {
                        template: '<review-match-score-for-user/>'
                    }).
                    when('/my/matches/play/:tournamentId', {
                        template: '<participant-match-list-to-judge/>'
                    }).
                    when('/match/state/description', {
                        template: '<match-state-description/>'
                    }).
                    when('/my/tournament/:tournamentId/category/:categoryId/members', {
                        template: '<tr-category-member-list/>'
                    }).
                    when('/my/tournament/edit/:tournamentId', {
                        template: '<tournament-properties-editor/>'
                    }).
                    when('/tournament/match/new-dispute/:tournamentId/:matchId', {
                        template: '<match-result-new-dispute/>'
                    }).
                    when('/my/tournament/parameters/:tournamentId', {
                        template: '<tr-parameters-editor/>'
                    }).
                    when('/participant/result/:tournamentId/:participantId', {
                        template: '<participant-result/>'
                    }).
                    when('/my/tournament/:tournamentId/change-category/:participantId', {
                        template: '<tr-category-membery-switcher/>'
                    }).
                    when('/my/matches/judgement/:tournamentId', {
                        template: '<judge-match-list-to-judge/>'
                    }).
                    when('/watch/tournament/:tournamentId', {
                        template: '<watch-tournament/>'
                    }).
                    when('/tournament/new', {
                        template: '<new-tournament/>'
                    }).
                    when('/tournament/copy/:tournamentId', {
                        template: '<copy-tournament/>'
                    }).
                    when('/tournament/new/parameters', {
                        template: '<new-tournament-parameters/>'
                    }).
                    when('/my/new/place', {
                        template: '<new-place/>'
                    }).
                    when('/place/:placeId', {
                        template: '<place-detail/>'
                    }).
                    when('/my-place/:placeId', {
                        template: '<my-place/>'
                    }).
                    when('/my-place/edit/:placeId', {
                        template: '<my-place-edit/>'
                    }).
                    when('/my-place/tables/:placeId', {
                        template: '<my-table-list/>'
                    }).
                    when('/pick/place', {
                        template: '<place-picker/>'
                    }).
                    when('/my/tournament/presence/:tournamentId', {
                        template: '<par-state-mgmt-list/>'
                    }).
                    when('/my/tournament/:tournamentId/participant/:userId', {
                        template: '<manage-one-participant/>'
                    }).
                    when('/my/tournament/categories/:tournamentId', {
                        template: '<tournament-category-list/>'
                    }).
                    when('/my/places', {
                        template: '<place-list/>'
                    }).
                    when('/my/tournaments', {
                        template: '<manage-tournament-list/>'
                    }).
                    when('/sign-up', {
                        template: '<sign-up/>'
                    }).
                    when('/sign-in', {
                        template: '<sign-in/>'
                    }).
                    when('/do-sign-in/:oneTimeSignInToken/:email', {
                        template: '<do-sign-in/>'
                    }).
                    when('/account', {
                        template: '<account/>'
                    }).
                    when('/account/profile/edit', {
                        template: '<account-edit/>'
                    }).
                    when('/participant/profile/:tournamentId/:participantId', {
                        template: '<participant-profile/>'
                    }).
                    when('/tournaments/:tournamentId', {
                        template: '<enlist-online/>'
                    }).
                    when('/tournament/:tournamentId/rank/manually/cid/:categoryId', {
                        template: '<rank-bid-manually/>'
                    }).
                    when('/tournament/result/:tournamentId', {
                        template: '<tournament-result/>'
                    }).
                    when('/my/tournament/:tournamentId', {
                        template: '<tournament-management/>'
                    }).
                    when('/tournament/enlist/offline/:tournamentId', {
                        template: '<enlist-offline/>'
                    }).
                    when('/default/route', {
                        template: '<default-route/>'
                    }).
                    otherwise('/default/route');
            }
           ]);
