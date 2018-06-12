import angular from 'angular';
import './tables.scss';
import template from './tables.template.html';

angular.
    module('place').
    component('myTableList', {
        templateUrl: template,
        controller: [
            'mainMenu', 'auth', 'requestStatus', 'Place', 'Table', '$routeParams', 'binder', '$scope',
            function (mainMenu, auth, requestStatus, Place, Table, $routeParams, binder, $scope) {
                var self = this;
                this.place = null;
                this.tables = null;
                this.newTables = 1;
                this.newTablesOptions = {min: 1, max: 50};
                this.sortLabelInt = function(table) {
                    return parseInt(table.label);
                };
                this.removeTable = function (idx) {
                    var table = self.tables[idx];
                    requestStatus.startLoading("Archiving table");
                    Table.setState(
                        {tableId: table.id,
                         pid: $routeParams.placeId,
                         expected: 'Free',
                         target: 'Archived'
                        },
                        function (ok) {
                            self.tables.splice(idx, 1);
                            requestStatus.complete()
                        },
                      (...a) => requestStatus.failed(...a));
                };
                this.addTables = function () {
                    self.form.$setSubmitted();
                    if (!self.form.$valid) {
                        return;
                    }
                    requestStatus.startLoading("Adding tables");
                    Table.add({placeId: $routeParams.placeId,
                               quantity: self.newTables},
                              function (ok) {
                                  requestStatus.complete();
                                  self.load();
                                  self.newTables = 1;
                              },
                            (...a) => requestStatus.failed(...a));
                };
                this.load = function () {
                    requestStatus.startLoading();
                    Place.tables({placeId: $routeParams.placeId},
                                 function (tables) {
                                     requestStatus.complete();
                                     self.tables = tables;
                                 },
                               (...a) => requestStatus.failed(...a));
                };
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Place Tables'),
                    'event.request.status.ready': (event) => self.load()});
            }
        ]});
