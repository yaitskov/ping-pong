import angular from 'angular';

angular.
    module('core.casting').
    factory('Casting', ['$resource',
                         function ($resource) {
                             return $resource('/api/category', {}, {
                                 manualBidsOrder: {
                                     url: '/api/casting-lots/manual-bids-order/tid/:tournamentId/cid/:categoryId',
                                     method: 'GET',
                                     cache: false,
                                     headers: {
                                         session: 1
                                     },
                                     isArray: true
                                 },
                                 orderBidsManually: {
                                     url: '/api/casting-lots/order-bids-manually',
                                     method: 'POST',
                                     headers: {
                                         session: 1
                                     },
                                     isArray: true
                                 }
                             });
                         }
                        ]);
