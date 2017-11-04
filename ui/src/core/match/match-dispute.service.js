import angular from 'angular';

angular.
    module('core.match').
    factory('MatchDispute', ['$resource',
                            function ($resource) {
                                return $resource('/api/dispute', {}, {
                                    openDispute: {
                                        url: '/api/dispute/open',
                                        method: 'POST',
                                        headers: {
                                            session: 1
                                        }
                                    }
                                });
                            }
                           ]);
