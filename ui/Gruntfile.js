// rm -rf node_modules && npm cache clean && npm i
// npm install grunt-contrib-concat --save-dev
// npm install grunt-dependency-installer --save-dev
// npm install grunt-install-dependencies --save-dev
// npm install grunt-html-build --save-dev
// npm install grunt-contrib-sass --save-dev
module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-install-dependencies');
    grunt.loadNpmTasks('grunt-contrib-sass');
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json')
    });
    grunt.initConfig({
        sass: {
            dist: {
                options: {
                    style: 'expanded'
                },
                files: {
                    'node_modules/app/app.css': 'src/app.scss'
                }
            }
        }
    });
    grunt.registerTask('all', ['install-dependencies', 'sass']);
    grunt.registerTask('default', ['all']);
};
