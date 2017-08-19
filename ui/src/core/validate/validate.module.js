import angular from 'angular';

import email from './email.validator.js';
import phone from './phone.validator.js';
import price from './price.validator.js';

angular.module('core.validate',
               [
                   'core.validate.email',
                   'core.validate.phone',
                   'core.validate.price'
               ]);
