import angular from 'angular';

angular.
    module('core.group').
    factory('groupSchedule', [function () {
        return new function () {
            var self = this;
            this.convertToText = function (participant2Matches) {
               var result = [];
               Object.keys(participant2Matches).forEach(function (n) {
                   var list = participant2Matches[n];
                   var pairs = [];
                   for (var i = 0; i < list.length; i += 2) {
                      pairs.push((parseInt(list[i]) + 1) + "-" + (1 + parseInt(list[i + 1])));
                   }
                   result.push("" + n + ": " + pairs.join(", ") + "\n")
               });
               return result.join("");
            };

            this.parseText = function (txt) {
                var result = {};
                if (!txt) {
                   return result;
                }
                var lines = txt.split("\n").
                   map(function (l) { return l.trim(); }).
                   filter(function (l) { return l; });
                for (var i = 0; i < lines.length; ++i) {
                   var line = lines[i];
                   var colonIdx = line.indexOf(":");
                   if (colonIdx < 1) {
                      throw {template: "Line has no colon",
                             templateParams: {line: line}};
                   }
                   var numOfParticipants = line.substr(0, colonIdx);
                   if (!numOfParticipants.match(/^ *[0-9]+ *$/)) {
                      throw {}
                   }
                   var indexes = [];
                   var pairs = line.substr(colonIdx + 1).split(',').
                       map(function (p) {return p.trim();}).
                       filter(function (p) { return p;});
                   for (var j = 0; j < pairs.length; ++j) {
                       var match = pairs[j].match(/^ *([0-9]+) *- *([0-9]+) *$/);
                       if (!match) {
                          throw {template: "Line has wrong match",
                                 templateParams: {line: line, match: pairs[j]}};
                       }
                       indexes.push(parseInt(match[1]) - 1, parseInt(match[2]) - 1);
                   }
                   result[parseInt(numOfParticipants)] = indexes;
                }
                return result;
            };

            this.formatScheduleError = function (templateParams) {
                templateParams.matches = templateParams.matches.
                    map(function (match) { return (match[0] + 1) + "-" + (match[1] + 1); }).
                    join(", ");
                return templateParams;
            };
        };
    }]);
