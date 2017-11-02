var merge = require('merge');

module.exports = merge.apply(
    null,
    [{},
     require('./pl.participant.result.js'),
     require('./pl.errors.js'),
     require('./pl.rank.manual.js'),
     require('./pl.common.js'),
     require('./pl.copy-tournament.js'),
     require('./drafting.js'),
     require('./main-menu.js'),
     require('./pl.request-status.js'),
     require('./account-page.js'),
     require('./sign-up-page.js'),
     require('./sign-in-page.js'),
     require('./do-authentication-page.js'),
     require('./edit-account-page.js'),
     require('./account.js'),
     require('./pl.participant.js'),
     require('./pl.tournament-online-draft.js'),
     require('./tournament-result.js'),
     require('./play-in-tournament-list.js'),
     require('./place-picker.js'),
     require('./place-detail.js'),
     require('./participant-presence.js'),
     require('./open-tournament-list.js'),
     require('./open-tournament-page.js'),
     require('./new-tournament.js'),
     require('./pl.new-tournament-parameters.js'),
     require('./new-place.js'),
     require('./my-tournament-list.js'),
     require('./my-tournament.js'),
     require('./my-tournament-participant.js'),
     require('./my-tournament-parameters.js'),
     require('./my-tournament-offline-enlist.js'),
     require('./my-place.js'),
     require('./my-place-edit.js'),
     require('./pl.my-tournament-categories.js'),
     require('./my-tournament-categories-change.js'),
     require('./my-tournament-categories-members.js'),
     require('./my-place-tables.js'),
     require('./-my-place-edit.js'),
     require('./pl.my-match-play-list.js'),
     require('./match-state-description.js'),
     require('./pl.my-match-judge-list.js'),
     require('./pl.complete-match.js'),
     require('./pl.complete-my-match.js')]);
