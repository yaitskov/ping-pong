export default class FileReaderField {
    constructor() {
        this.restrict = 'A';
        this.require = 'ngModel';
        this.scope = {ngModel: '='};
    }

    link(scope, element, attrs, ngModel) {
        element.bind("change", function (changeEvent) {
            const reader = new FileReader();
            reader.onload = (loadEvent) => scope.$apply(
                () => ngModel.$setViewValue(loadEvent.target.result));
            reader.readAsText(changeEvent.target.files[0]);
        });
    }
}
