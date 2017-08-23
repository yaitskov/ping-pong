import angular from 'angular';
import template from './enlist.template.html';

angular.
    module('enlistOffline').
    component('enlistOffline', {
        templateUrl: template,
        cache: false,
        controller: ['$routeParams', 'Tournament', 'mainMenu', '$translate',
                     'requestStatus', 'Participant', '$http', 'auth', 'pageCtx',
                     function ($routeParams, Tournament, mainMenu, $translate,
                               requestStatus, Participant, $http, auth, pageCtx) {
                         $translate('Offline enlist').then(function (msg) {
                             mainMenu.setTitle(msg);
                         });
                         var self = this;
                         self.categoryId = null;
                         self.tournamentId = $routeParams.tournamentId;
                         self.categories = null;
                         self.enlisted = [];
                         self.form = {};
                         $translate('Loading').then(function (msg) {
                             requestStatus.startLoading(msg);
                             Tournament.aDrafting(
                                 {tournamentId: $routeParams.tournamentId},
                                 function (tournament) {
                                     requestStatus.complete();
                                     $translate('Offline enlist to', {name: tournament.name}).then(function (msg) {
                                         mainMenu.setTitle(msg);
                                     });
                                     self.categories = tournament.categories;
                                     var wasCategoryId = pageCtx.get('offline-category-' + $routeParams.tournamentId);
                                     for (var i in tournament.categories) {
                                         if (!wasCategoryId || wasCategoryId == tournament.categories[i].cid) {
                                             self.categoryId = tournament.categories[i].cid;
                                             break;
                                         }
                                     }
                                     if (!self.categoryId) {
                                         for (var i in tournament.categories) {
                                             self.categoryId = tournament.categories[i].cid;
                                             break;
                                         }
                                     }
                                 },
                                 requestStatus.failed);
                         });

                         this.activate = function (cid) {
                             self.categoryId = cid;
                             pageCtx.put('offline-category-' + $routeParams.tournamentId, self.categoryId);
                         };

                         this.enlist = function (bidState) {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             $translate('Enlisting').then(function (msg) {
                                 requestStatus.startLoading(msg, self.tournament);
                                 if (!self.categoryId) {
                                     $translate("CategoryNotChosen").then(function (msg) {
                                         requestStatus.validationFailed(msg);
                                     });
                                     return;
                                 }
                                 var name = self.firstName + ' ' + self.lastName;
                                 $http.post('/api/tournament/enlist-offline',
                                            {tid: self.tournamentId,
                                             cid: self.categoryId,
                                             bidState: bidState,
                                             name: name
                                            },
                                            {headers: {session: auth.mySession()}}).
                                     then(
                                         function (resp) {
                                             requestStatus.complete();
                                             self.firstName = '';
                                             self.lastName = '';
                                             self.enlisted.unshift({uid: resp.data, name: name});
                                             self.form.$setPristine(true);
                                             jQuery(self.form.firstName.$$element).focus();
                                         },
                                         requestStatus.failed);
                             });
                         };
                     }
                                     ]
    });
