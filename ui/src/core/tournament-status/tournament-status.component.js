'use strict';

angular.
    module('core.tournamentStatus').
    component('tournamentStatus', {
        templateUrl: 'core/tournament-status/tournament-status.template.html',
        controller: [
            'tournamentStatus', '$rootScope',
            function (tournamentStatus, $rootScope) {
                this.reset = function () {
                    this.tournament = {};
                };
                this.meta = null;
                var self = this;
                self.reset();
                $rootScope.$on('event.request.started', function (event, msg, meta) {
                    self.reset();
                    self.meta = meta;
                });
                $rootScope.$on('event.request.failed', function (event, response) {
                    self.reset();
                    if (response.status == 400 &&
                        typeof response.data == 'object' &&
                        response.data.error == 'BadState') {
                        self.tournament = {tid: self.tournament.tid,
                                           name: self.tournament.name,
                                           state: bo.data.state};
                    }
                });
                $scope.$on('event.request.complete', function (event, response) {
                    self.reset();
                });
            }]});
