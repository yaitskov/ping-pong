import angular from 'angular';
import template from './tournament-status.template.html';

angular.
    module('core.tournamentStatus').
    component('tournamentStatus', {
        templateUrl: template,
        controller: ['$rootScope', function ($rootScope) {
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
            $rootScope.$on('event.request.failed', function (event, response, meta) {
                self.reset();
                if (response.status == 400 &&
                    typeof response.data == 'object' &&
                    response.data.error == 'BadState') {
                    if (meta) {
                        self.meta = meta;
                    }
                    self.tournament = {tid: self.meta.tid,
                                       name: self.meta.name,
                                       state: response.data.state};
                }
            });
            $rootScope.$on('event.request.complete', function (event, response) {
                self.reset();
            });
        }]});
