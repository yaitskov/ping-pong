import angular from 'angular';
import template from './enlist.template.html';

angular.
    module('enlistOffline').
    component('enlistOffline', {
        templateUrl: template,
        cache: false,
        controller: ['$routeParams', 'Tournament', 'mainMenu',
                     'requestStatus', 'Participant', '$http', 'auth', 'pageCtx',
                     function ($routeParams, Tournament, mainMenu,
                               requestStatus, Participant, $http, auth, pageCtx) {
                         mainMenu.setTitle('Offline enlist');
                         var self = this;
                         self.categoryId = null;
                         self.tournamentId = $routeParams.tournamentId;
                         self.categories = null;
                         self.enlisted = [];
                         self.form = {};
                         requestStatus.startLoading('Loading');
                         Tournament.aDrafting(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 mainMenu.setTitle(['Offline enlist to', {name: tournament.name}]);
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


                         this.activate = function (cid) {
                             self.categoryId = cid;
                             pageCtx.put('offline-category-' + $routeParams.tournamentId, self.categoryId);
                         };

                         this.enlist = function (bidState) {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Enlisting', self.tournament);
                             if (!self.categoryId) {
                                 requestStatus.validationFailed("CategoryNotChosen");
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

                         };
                     }
                                 ]
    });
