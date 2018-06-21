import angular from 'angular';
import manyButtonToggler from './ manyButtonToggler.js';

angular.module('core.ui').
    /**
       <many-button-toggler domain="['su']" ng-model="$ctrl.rules.casting.policy"
                            selected-class="btn-primary"
                            label="rank-policy-lbl"/>

       ===>

       <div class="form-group" id="idx">
           <label class="toggle-option" translate="rank-policy-lbl"/>
           <a class="btn btn-default"
              translate="sign-up-policy"
              ng-click="$ctrl.rules.casting.policy = 'su'"
              ng-class="{'btn-primary': $ctrl.rules.casting.policy == 'su'}"/>
       </div>

    */
    directive('manyButtonToggler', manyButtonToggler);
