package org.dan.ping.pong.sys.validation;

import org.dan.ping.pong.app.tournament.Tid;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TidBodyRequired.Validator.class)
public @interface TidBodyRequired {
    String REQUEST_BODY_MUST_BE_A_TOURNAMENT_ID = "request body must be a tournament id";

    String message() default REQUEST_BODY_MUST_BE_A_TOURNAMENT_ID;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<TidBodyRequired, Tid> {
        public void initialize(final TidBodyRequired hasId) {
        }

        public boolean isValid(final Tid tid, final ConstraintValidatorContext constraintValidatorContext) {
            return tid != null;
        }
    }
}
