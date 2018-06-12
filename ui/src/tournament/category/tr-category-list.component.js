import angular from 'angular';
import './tr-category-list.scss';
import template from './tr-category-list.template.html';

angular.module('tournamentCategory').
    component('tournamentCategoryList', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus', 'pageCtx', '$location', 'binder', '$scope',
                     function ($http, mainMenu, $routeParams, auth, requestStatus, pageCtx, $location, binder, $scope) {
                         this.tournament = pageCtx.get('tournamentInfoForCategories') || {tid: $routeParams.tournamentId};
                         this.categories = null;
                         this.newCategoryName = '';
                         var self = this;
                         this.beginDraft = function () {
                             requestStatus.startLoading("Openning draft");
                             $http.post('/api/tournament/state',
                                        {tid: self.tournament.tid, state: 'Draft'},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         requestStatus.complete();
                                         self.tournament.state = 'Draft';
                                         pageCtx.put('tournamentInfoForCategories',
                                                     {tid: self.tournament.tid,
                                                      name: self.tournament.name,
                                                      state: self.tournament.state});
                                         $location.path('/my/tournament/' + self.tournament.tid);
                                     },
                                   (...a) => requestStatus.failed(...a));
                         };
                         this.addGroup = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Adding category');
                             $http.post('/api/category/create',
                                        {tid: $routeParams.tournamentId,
                                         name: self.newCategoryName},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (ok) {
                                         self.categories.push({cid: ok.data, name: self.newCategoryName});
                                         self.newCategoryName = '';
                                         requestStatus.complete();
                                         self.form.$setPristine(true);
                                     },
                                   (...a) => requestStatus.failed(...a));
                         };
                         this.removeCategoryByIdx = function (idx) {
                             requestStatus.startLoading('Removing category');
                             var category = this.categories[idx];
                             $http.post('/api/category/delete/' + category.cid, {},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (ok) {
                                         requestStatus.complete();
                                         self.categories.splice(idx, 1);
                                     },
                                   (...a) => requestStatus.failed(...a));
                         };
                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 var ctxMenu = {};
                                 ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                                 mainMenu.setTitle('Categories', ctxMenu);
                             },
                             'event.request.status.ready': (e) => {
                                 requestStatus.startLoading('Load categories');
                                 $http.get('/api/category/find/by/tid/' + $routeParams.tournamentId).
                                     then(
                                         function (okResp) {
                                             requestStatus.complete();
                                             self.categories = okResp.data;
                                         },
                                       (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
        });
