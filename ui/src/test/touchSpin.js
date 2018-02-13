function checkTouchSpin(ctx, jqueryPrefix, valueF, expectedValue, jquerySuffix) {
    const touchSpin = ctx.element.find(
        `${jqueryPrefix} button[ng-class="vm.touchSpinOptions.${jquerySuffix}"]`);
    touchSpin.mousedown();
    touchSpin.mouseup();
    ctx.sync();
    expect(valueF(ctx)).toBe(expectedValue);
}

export function checkTouchSpinDecrease(ctx, jqueryPrefix, valueF) {
    checkTouchSpin(ctx, jqueryPrefix, valueF,
                   valueF(ctx) - 1, "buttonDownClass");
}

export function checkTouchSpinIncrease(ctx, jqueryPrefix, valueF) {
    checkTouchSpin(ctx, jqueryPrefix, valueF,
                   valueF(ctx) + 1, "buttonUpClass");
}

export function checkTouchSpinNotDecrease(ctx, jqueryPrefix, valueF) {
    checkTouchSpin(ctx, jqueryPrefix, valueF,
                   valueF(ctx), "buttonDownClass");
}

export function checkTouchSpinNotIncrease(ctx, jqueryPrefix, valueF) {
    checkTouchSpin(ctx, jqueryPrefix, valueF,
                   valueF(ctx), "buttonUpClass");
}
