'use strict';

angular.
    module('core.participant').
    factory('Participant', ['$resource',
                            function ($resource) {
                                return $resource('/api/bid', {}, {
                                    state: {
                                        url: '/api/bid/state/:tournamentId/:uid',
                                        method: 'GET',
                                        cache: false,
                                        headers: {
                                            session: 1
                                        }
                                    },
                                    setCategory: {
                                        url: '/api/bid/set-category',
                                        method: 'POST',
                                        cache: false,
                                        headers: {
                                            session: 1
                                        }
                                    }
                                });
                            }
                           ]);
