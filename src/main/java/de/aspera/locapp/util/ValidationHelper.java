package de.aspera.locapp.util;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author daniel
 */
public class ValidationHelper {

    private static final Logger logger = Logger.getLogger(ValidationHelper.class.getName());
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,4})$";

    public static boolean validEmail(String email) {
        if (email != null && email.matches(EMAIL_PATTERN)) {
            return true;
        }
        return false;
    }

    public static void validateBean(Object obj) throws RuntimeException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(obj);
        StringBuilder build = new StringBuilder();
        for (ConstraintViolation<?> object : constraintViolations) {
            build.append("\n-- Invalid Values for entity beans --\n");
            build.append("Invalid value: ").append(object.getInvalidValue()).append("\n");
            build.append("Invalid Object: ").append(object.getRootBean()).append("\n");
            build.append("Invalid Class: ").append(object.getRootBeanClass()).append("\n");
            build.append("\n--\n raw value:").append(object.toString()).append("\n--\n");
        }

        if (constraintViolations != null && constraintViolations.size() >= 1) {
            logger.log(Level.SEVERE, build.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
