'use strict';

angular.
    module('core.tournamentStatus').
    component('tournamentStatus', {
        templateUrl: 'core/tournament-status/tournament-status.template.html',
        controller: ['$rootScope', function ($rootScope) {
            this.reset = function () {
                this.tournament = {};
            };
            this.meta = {};
            var self = this;
            self.reset();
            $rootScope.$on('event.request.started', function (event, msg, meta) {
                self.reset();
                self.meta = meta;
            });
            $rootScope.$on('event.request.failed', function (event, response, meta) {
                self.reset();
                if (response.status == 400 &&
                    typeof response.data == 'object' &&
                    response.data.error == 'BadState') {
                    self.tournament = {tid: self.meta.tid || meta.tid,
                                       name: self.meta.name,
                                       state: response.data.state};
                }
            });
            $rootScope.$on('event.request.complete', function (event, response) {
                self.reset();
            });
        }]});
