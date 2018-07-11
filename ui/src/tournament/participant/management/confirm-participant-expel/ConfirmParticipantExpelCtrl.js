import SimpleController from 'core/angular/SimpleController.js';

export default class ConfirmParticipantExpelCtrl extends SimpleController {
    static get $inject () {
        return ['binder', '$scope', '$rootScope', '$element', 'Tournament', 'AjaxInfo'];
    }

    $onInit() {
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.binder(this.$scope, {
            'event.confirm-participant-expel.confirm': (...args) => this.confirm(...args)
        });
        this.$rootScope.$broadcast('event.confirm-participant-expel.ready');
    }

    confirm(event, bid) {
        this.$element.find('#confirmParticipantExpel').modal('show');
        this.bid = bid;
    }

    expelAs(expelAs) {
        this.ajax.doAjax(
            'Expelling',
            this.Tournament.expel,
            {bid: this.bid.bid,
             tid: this.bid.tid,
             targetBidState: expelAs},
            (ok) => this.bid.state = expelAs);
    }

    expelUrl(tid, bid) {
        return `#!/my/tournament/${tid}/participant/${bid}`;
    }
}
