import angular from 'angular';
import ConsoleTournamentRuleLink from './ConsoleTournamentRuleLink.js';
import injectableDirective from 'core/angular/injectableDirective.js';

angular.module('tournament').
    directive('consoleTournamentRuleLink', injectableDirective(ConsoleTournamentRuleLink));
