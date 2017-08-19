import angular from 'angular';

angular.
    module('core.user').
    factory('User', ['$resource',
                            function ($resource) {
                                return $resource('/api/user/profile/update', {}, {
                                    change: {
                                        url: '/api/user/profile/update',
                                        method: 'POST',
                                        headers: {
                                            session: 1
                                        }
                                    }
                                });
                            }
                    ]);
