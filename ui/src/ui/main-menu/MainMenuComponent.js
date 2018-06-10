import AngularBean from 'core/angular/AngularBean.js';

export default class MainMenuComponent extends AngularBean {
    static get $inject() {
        return ['Facebook', 'auth', 'mainMenu', '$rootScope',
                'binder', '$scope', '$window', 'pageCtx', 'MessageBus'];
    }

    $onInit() {
        this.accountName = this.auth.myName();
        this.title = this.mainMenu.getTitle();
        this.lastTournament = this.pageCtx.get('last-tournament');
        this.setMenu(this.mainMenu.getContextMenu());

        this.binder(this.$scope, {
            'event.mm.last.tournament': (event, tournament) => {
                this.pageCtx.put('last-tournament', tournament);
                this.lastTournament = tournament;
            }
            //'menu.set': (event, menu) => this.setMenu(menu),
            //'title.set': (event, title) => this.setTitle(title)
        });
        this.MessageBus.subscribeIn(this.$scope, 'title.set',
                                    (title) => this.setTitle(title));
        this.MessageBus.subscribeIn(this.$scope, 'menu.set',
                                    (menu) => this.setMenu(menu));
        this.$rootScope.$broadcast('event.main.menu.ready');
    }

    setTitle(title) {
        this.$window.document.title = title;
        this.title = title;
    }

    setMenu(menu) {
        this.contextMenu = menu;
    }

    isAuthenticated() {
        return this.auth.isAuthenticated();
    }

    loginFb() {
        this.Facebook.loginStatus((r) => {
            if (r.status == 'connected') {
                this.Facebook.checkPermissions(
                    r.authResponse.userID,
                    (r) => {
                        console.log("permission ok " + r);
                    });
            } else {
                this.Facebook.login((r) => {
                    console.log("login ok" + r);
                });
            }
        });
    }

    isAdmin() {
        return this.auth.userType() == 'Admin' || this.auth.userType() == 'Super';
    }
}
