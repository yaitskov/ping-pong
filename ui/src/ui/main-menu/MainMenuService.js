import AngularBean from 'core/angular/AngularBean.js';

export default class MainMenuService extends AngularBean {
     static get $inject() {
         return ['$rootScope', '$timeout', 'syncTranslate', 'MessageBus'];
     }

     constructor(...args) {
         super(...args);
         this.stranslate = this.syncTranslate.create();
         this.stranslateMenu = this.syncTranslate.create();
         this.title = '....';
         this.contextMenu = {};
     }

    setTitle(originTitle, menu) {
        this.contextMenu = {};
        if (menu) {
            this.setContextMenu(menu);
        }

        this.stranslate.trans(originTitle || 'Loading', (title) => {
            this.title = title;
            //this.$rootScope.$broadcast('title.set', title);
            this.MessageBus.broadcast('title.set', title);
        });
    }

    getTitle() {
        return this.title;
    }

    setContextMenu(originMenu) {
        this.stranslateMenu.transMenu(originMenu, (menu) => {
            this.contextMenu = menu;
            //this.$rootScope.$broadcast('menu.set', menu);
            this.MessageBus.broadcast('menu.set', menu);
        });
    }

    getContextMenu() {
        return this.contextMenu;
    }
}
