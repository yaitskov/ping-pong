// rm -rf node_modules && npm cache clean && npm i
// npm install grunt-contrib-concat --save-dev
// npm install grunt-dependency-installer --save-dev
// npm install grunt-install-dependencies --save-dev
// npm install grunt-html-build --save-dev
module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-install-dependencies');
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
    });
    grunt.registerTask('all', ['install-dependencies']);
    grunt.registerTask('default', ['all']);
};
