import angular from 'angular';

angular.module('newPlace', ['ngRoute', 'mainMenu', 'auth',
                            'core.city', 'core.country',
                            'localStorage',
                            'core.requestStatus', 'core.validate']);
