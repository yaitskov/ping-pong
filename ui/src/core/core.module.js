import angular from 'angular';

import './util.service.js';
import './ui.directive.js';
import './validate/validate.module.js';

import './request-status/request-status.module.js';
import './request-status/request-status.service.js';
import './request-status/request-status.component.js';

import './tournament-status/tournament-status.module.js';
import './tournament-status/tournament-status.component.js';

import './local-storage/local-storage.module.js';
import './local-storage/local-storage.service.js';

import './group/group.module.js';
import './group/group.service.js';
import './group/group.schedule.service.js';

import './place/place.module.js';
import './place/place.service.js';

import './table/table.module.js';
import './table/table.service.js';

import './match/match.module.js';
import './match/match.service.js';
import './match/match-dispute.service.js';

import './user/user.module.js';
import './user/user.service.js';

import './country/country.module.js';
import './country/country.service.js';

import './city/city.module.js';
import './city/city.service.js';

import './tournament/tournament.module.js';
import './tournament/tournament.service.js';

import './category/category.module.js';
import './category/category.service.js';

import './casting/casting.module.js';
import './casting/casting.service.js';

import './participant/participant.module.js';
import './participant/participant.service.js';

angular.module('core', ['core.tournament', 'core.match',
                        'core.util', 'core.group', 'core.casting',
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
