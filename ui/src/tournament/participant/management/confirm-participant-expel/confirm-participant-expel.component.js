import angular from 'angular';
import template from './confirm-participant-expel.template.html';

angular.module('participant').
    component('confirmParticipantExpel', {
        templateUrl: template,
        controller: ['binder', '$scope', '$rootScope', '$element', 'requestStatus', 'Tournament',
        function (binder, $scope, $rootScope, $element, requestStatus, Tournament) {
            const self = this;
            self.confirm = (event, bid) => {
                $element.find('#confirmParticipantExpel').modal('show');
                self.bid = bid;
            };
            self.expelAs = (expelAs) => {
                requestStatus.startLoading('Expelling');
                Tournament.expel(
                    {bid: self.bid.user.bid,
                     tid: self.bid.tid,
                     targetBidState: expelAs},
                    (ok) => {
                        requestStatus.complete();
                        self.bid.state = expelAs;
                    },
                  (...a) => requestStatus.failed(...a));
            };
            self.expelUrl = (tid, uid) => `#!/my/tournament/${tid}/participant/${uid}`;
            binder($scope, {
                'event.confirm-participant-expel.confirm': self.confirm
            });
            $rootScope.$broadcast('event.confirm-participant-expel.ready');
        }]
    });
