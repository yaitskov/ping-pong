'use strict';

angular.module('pingPong').
    config(['$locationProvider', '$routeProvider',
            function config($locationProvider, $routeProvider) {
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
                    when('/complete/match/my/:matchId', {
                        template: '<complete-my-match></complete-my-match>'
                    }).
                    when('/my/matches/play', {
                        template: '<my-match-play-list></my-match-play-list>'
                    }).
                    when('/watch/tournament/:tournamentId', {
                        template: '<open-tournament></open-tournament>'
                    }).
                    when('/tournament/new', {
                        template: '<new-tournament></new-tournament>'
                    }).
                    when('/my/new/place', {
                        template: '<new-place></new-place>'
                    }).
                    when('/place/:placeId', {
                        template: '<place-detail></place-detail>'
                    }).
                    when('/pick/place', {
                        template: '<place-picker></place-picker>'
                    }).
                    when('/my/tournament/presence/:tournamentId', {
                        template: '<participant-presence></participant-presence>'
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
                    when('/tournaments/:tournamentId', {
                        template: '<tournament-detail></tournament-detail>'
                    }).
                    when('/my/tournament/:tournamentId', {
                        template: '<my-tournament></my-tournament>'
                    }).
                    otherwise('/tournaments');
            }
           ]);
