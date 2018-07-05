import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './param.form.template.html';
import TournamentRulesCtrl from './TournamentRulesCtrl.js';

import './seeding/seeding-tr-params.component.js';
import './console/console-tr-params.component.js';
import './group/headless-dm-match-params.component.js';
import './group/group-rule-parameters-dialog.component.js';
import './group/pick-group-rules-dialog.component.js';
import './group/group-order-rules.component.js';
import './group/group-tr-params.component.js';
import './play-off/play-off-tr-params.component.js';
import './match/headless-common-match-params.component.js';
import './match/match-params.component.js';
import './arena/arena-params.component.js';
import './enlist/enlist-params.component.js';

angular.
    module('tournament').
    component('tournamentParametersForm', {
        templateUrl: template,
        controller: TournamentRulesCtrl});
