import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOfflineCtrl extends SimpleController {
    static get $inject () {
        return ['mainMenu', '$routeParams', '$scope',
                'AjaxInfo', 'Group', 'Participant'];
    }

    $onInit() {
        this.groups = null;
        this.bid = null;
        this.tournamentId = this.$routeParams.tournamentId;
        this.participantId = this.$routeParams.participantId;

        const ctxMenu = {};
        ctxMenu['#!/my/tournament/' + this.tournamentId] = 'Tournament';
        this.mainMenu.setTitle('Change group', ctxMenu);

        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.ajax.doAjax('Loading groups', this.Group.list,
                         {tournamentId: this.tournamentId},
                         (r) => this.groups = r.groups);
        this.ajax.doAjax('Load participant', this.Participant.profile,
                         {tournamentId: this.tournamentId,
                          participantId: this.participantId},
                         (bid) => this.bid = bid);
    }

    assignGroup(targetGid) {
        if (targetGid == this.bid.group.gid) {
            history.back();
            return;
        }
        let req = {expectedGid: this.bid.group.gid,
                   tid: this.tournamentId,
                   bid: this.participantId};
        if (targetGid !== 0) {
            req.targetGid = targetGid;
        }
        this.ajax.doAjax(
            'Changing group',
            this.Participant.changeGroup,
            req,
            () => history.back());
    }
}
