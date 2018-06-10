import angular from 'angular';
import FileReaderField from './FileReaderField.js';

angular.module('widget').
    directive('fileReader', () => new FileReaderField());
