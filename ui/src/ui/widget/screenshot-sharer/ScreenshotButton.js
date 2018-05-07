import AngularBean from 'core/angular/AngularBean.js';
import Html2Canvas from 'html2canvas/dist/html2canvas.min.js';
import ScreenSharerDialog from './ScreenSharerDialog.js';

export default class ScreenshotButton extends AngularBean {
    static get $inject() {
        return ['MessageBus', '$window'];
    }

    constructor(...args) {
        super(...args);
        this.restrict = 'E';
        this.scope = {
            targetAnchor: '@'
        };
        this.template = `<a ng-click="shootAndShowShareDialog()"
                            title="Share"
                            class="btn btn-primary">
                               <span class="glyphicon glyphicon-share"/>
                         </a>`;
    }

    shootAndShowShareDialog(targetAnchor) {
        console.log(`Shooting DOOM [${targetAnchor}]`);
        const dom = this.$window.document.getElementById(targetAnchor);
        Html2Canvas(dom).then(
            (canvas) => this.MessageBus.broadcast(
                           ScreenSharerDialog.TopicShow, canvas));
    }

    link(scope, element, attrs) {
        scope.shootAndShowShareDialog = () => this.shootAndShowShareDialog(scope.targetAnchor);
    }
}