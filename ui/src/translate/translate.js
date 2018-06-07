import applyAllToMap from './transformations.js';
var pl = require('./pl/pl.js');
var en = require('./en/en.js');

module.exports = {en: applyAllToMap(en), pl: applyAllToMap(pl)};
