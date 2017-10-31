var merge = require('merge');

module.exports = merge.apply(
    null,
    [{},
     require('./en.participant.result.js'),
     require('./en.errors.js'),
     require('./en.rank.manual.js'),
     require('./en.common.js'),
     require('./drafting.js'),
     require('./main menu.js'),
     require('./en.request-status.js'),
     require('./account page.js'),
     require('./sign up page.js'),
     require('./sign in page.js'),
     require('./do authentication page.js'),
     require('./edit account page.js'),
     require('./account.js'),
     require('./tournament details page.js'),
     require('./tournament result.js'),
     require('./play in tournament list.js'),
     require('./place picker.js'),
     require('./place detail.js'),
     require('./participant presence.js'),
     require('./open tournament list.js'),
     require('./open tournament page.js'),
     require('./new tournament.js'),
     require('./en.new-tournament-parameters.js'),
     require('./new place.js'),
     require('./my tournament list.js'),
     require('./my tournament.js'),
     require('./my tournament participant.js'),
     require('./my tournament parameters.js'),
     require('./my tournament offline enlist.js'),
     require('./my place.js'),
     require('./my place edit.js'),
     require('./en.my-tournament-categories.js'),
     require('./my tournament categories change.js'),
     require('./my tournament categories members.js'),
     require('./my place tables.js'),
     require('./ my place edit.js'),
     require('./en.my-match-play-list.js'),
     require('./match state description.js'),
     require('./en.my-match-judge-list.js'),
     require('./en.complete-match.js'),
     require('./en.copy-tournament.js'),
     require('./en.complete-my-match.js')]);
