'use strict';

angular.module('tournamentCategories').
    component('tournamentCategories', {
        templateUrl: 'my-tournament/categories/categories.template.html',
        controller: ['$http', 'mainMenu', '$routeParams', 'auth',
                     function ($http, mainMenu, $routeParams, auth) {
                         mainMenu.setTitle('Categories');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setContextMenu(ctxMenu);
                         this.categories = null;
                         this.error = null;
                         this.newCategoryName = '';
                         var self = this;
                         this.addGroup = function () {
                             this.error = null;
                             if (!self.newCategoryName) {
                                 this.error = 'Empty Group Name';
                                 return;
                             }
                             $http.post('/api/category/create',
                                        {tid: $routeParams.tournamentId,
                                         name: self.newCategoryName},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (ok) {
                                         self.categories.push({cid: ok.data, name: self.newCategoryName});
                                         self.newCategoryName = '';
                                         self.error = null;
                                     },
                                     function (bad) {
                                         self.error = " failed to add category " + bad;
                                     });
                         };
                         this.removeCategoryByIdx = function (idx) {
                             var category = this.categories[idx];
                             $http.post('/api/category/delete/' + category.cid, {},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (ok) {
                                         self.error = "";
                                         self.categories.splice(idx, 1);
                                     },
                                     function (bad) {
                                         self.error = "Failed to remove category " + category.name;
                                     });
                         };
                         $http.get('/api/category/find/by/tid/' + $routeParams.tournamentId).
                             then(
                                 function (okResp) {
                                     self.categories = okResp.data;
                                 },
                                 function (badResp) {
                                     self.error = "Failed to get catories " + badResp;
                                 });
                     }
                    ]
        });
