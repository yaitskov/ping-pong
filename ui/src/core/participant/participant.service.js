import angular from 'angular';

angular.
    module('core.participant').
    factory('Participant', ['$resource',
                            function ($resource) {
                                return $resource('/api/bid', {}, {
                                    profile: {
                                        url: '/api/bid/profile/:tournamentId/:participantId',
                                        method: 'GET'
                                    },
                                    state: {
                                        url: '/api/bid/state/:tournamentId/:uid',
                                        method: 'GET',
                                        cache: false,
                                        headers: {
                                            session: 1
                                        }
                                    },
                                    getResults: {
                                        url: '/api/bid/results/:tournamentId/:uid',
                                        method: 'GET',
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
                                    },
                                    findByState: {
                                        url: '/api/bid/find-by-state',
                                        method: 'POST',
                                        isArray: true
                                    },
                                    findWithMatch: {
                                        url: '/api/bid/find-with-match/:tournamentId',
                                        method: 'GET',
                                        isArray: true
                                    },
                                    setState: {
                                        url: '/api/bid/set-state',
                                        method: 'POST',
                                        cache: false,
                                        headers: {
                                            session: 1
                                        }
                                    }
                                });
                            }
                           ]);
