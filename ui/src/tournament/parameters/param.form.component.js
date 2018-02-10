import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './param.form.template.html';
import TournamentRulesCtrl from './TournamentRulesCtrl.js';

import './seeding/seeding-tr-params.component.js';
import './console/console-tr-params.component.js';
import './group/group-tr-params.component.js';
import './play-off/play-off-tr-params.component.js';
import './match/match-params.component.js';
import './arena/arena-params.component.js';

angular.
    module('tournament').
    component('tournamentParametersForm', {
        templateUrl: template,
        controller: TournamentRulesCtrl});
