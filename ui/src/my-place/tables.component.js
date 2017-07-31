'use strict';

angular.
    module('myTableList').
    component('myTableList', {
        templateUrl: 'my-place/tables.template.html',
        controller: [
            'mainMenu', 'auth', 'requestStatus', 'Place', 'Table', '$routeParams',
            function (mainMenu, auth, requestStatus, Place, Table, $routeParams) {
                mainMenu.setTitle('Place Tables');
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
                         expected: 'Free',
                         target: 'Archived'
                        },
                        function (ok) {
                            self.tables.splice(idx, 1);
                            requestStatus.complete()
                        },
                        requestStatus.failed);
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
                              requestStatus.failed);
                };
                this.load = function () {
                    requestStatus.startLoading();
                    Place.tables({placeId: $routeParams.placeId},
                                 function (tables) {
                                     requestStatus.complete();
                                     self.tables = tables;
                                 },
                                 requestStatus.failed);
                };
                self.load();
            }]});
