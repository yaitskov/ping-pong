import angular from 'angular';

import util from './util.service.js';
import ui from './ui.directive.js';
import validate from './validate/validate.module.js';

import requestStatusModule from './request-status/request-status.module.js';
import requestStatusService from './request-status/request-status.service.js';
import requestStatusComponent from './request-status/request-status.component.js';

import tournamentStatusModule from './tournament-status/tournament-status.module.js';
import tournamentStatusComponent from './tournament-status/tournament-status.component.js';

import localStorageModule from './local-storage/local-storage.module.js';
import localStorageService from './local-storage/local-storage.service.js';

import placeModule from './place/place.module.js';
import placeService from './place/place.service.js';

import tableModule from './table/table.module.js';
import tableService from './table/table.service.js';

import matchModule from './match/match.module.js';
import matchService from './match/match.service.js';


import userModule from './user/user.module.js';
import userService from './user/user.service.js';

import countryModule from './country/country.module.js';
import countryService from './country/country.service.js';

import cityModule from './city/city.module.js';
import cityService from './city/city.service.js';

import tournamentModule from './tournament/tournament.module.js';
import tournamentService from './tournament/tournament.service.js';

import categoryModule from './category/category.module.js';
import categoryService from './category/category.service.js';

import participantModule from './participant/participant.module.js';
import participantService from './participant/participant.service.js';

angular.module('core', ['core.tournament', 'core.match',
                        'core.util',
                        'localStorage', 'core.place',
                        'core.table', 'core.city', 'core.country',
                        'core.participant', 'core.user',
                        'core.validate', 'core.category',
                        'core.requestStatus', 'core.ui']).
    filter('isEmpty', function () {
        var bar;
        return function (obj) {
            for (bar in obj) {
                if (obj.hasOwnProperty(bar)) {
                    return false;
                }
            }
            return true;
        };
    });
