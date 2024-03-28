package de.fantasypixel.rework.framework.web;

import de.fantasypixel.rework.framework.UtilClasses;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class WebRouteValidatorTest {

    Class<WebRouteValidatorTest> clazz = WebRouteValidatorTest.class;
    WebRouteMatcher matcher = new WebRouteMatcher(Constants.logger);
    WebRouteValidator validator = new WebRouteValidator(Constants.logger, matcher);
    BiFunction<String, Object, WebResponse> voidHandler = (s, o) -> WebResponse.OK;

    WebResponse methodWithNoParams() { return WebResponse.OK; }
    WebResponse methodWithOneParam(String s) { return WebResponse.OK; }
    WebResponse methodWithOneWrongParam(int i) { return WebResponse.OK; }
    WebResponse methodWithTwoParams(String s, Object o) { return WebResponse.OK; }
    WebResponse methodWithTwoWrongParams(int i, Object o) { return WebResponse.OK; }
    WebResponse methodWithTwoWrongParams2(String s, int i) { return WebResponse.OK; }
    WebResponse methodWithThreeParams(String s, Object o, int i) { return WebResponse.OK; }
    String methodWithWrongReturnType() { return "Nope"; }

    @Test
    void testValidation() throws NoSuchMethodException {

        // check for wrong return / parameter types
        assertTrue(
                validator.validate(
                        "test-valid-no-params",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithNoParams")
                )
        );
        assertTrue(
                validator.validate(
                        "test-valid-one-param",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithOneParam", String.class)
                )
        );
        assertTrue(
                validator.validate(
                        "test-valid-two-params",
                        "/",
                        WebManager.HttpMethod.POST, // POST is used here because GET doesn't support body access (is tested below)
                        clazz.getDeclaredMethod("methodWithTwoParams", String.class, Object.class)
                )
        );
        assertFalse(
                validator.validate(
                        "test-invalid-one-param",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithOneWrongParam", int.class)
                )
        );
        assertFalse(
                validator.validate(
                        "test-invalid-two-params",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithTwoWrongParams", int.class, Object.class)
                )
        );
        assertFalse(
                validator.validate(
                        "test-invalid-two-params-2",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithTwoWrongParams2", String.class, int.class)
                )
        );
        assertFalse(
                validator.validate(
                        "test-invalid-three-params",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithThreeParams", String.class, Object.class, int.class)
                )
        );
        assertFalse(
                validator.validate(
                        "test-invalid-wrong-return-type",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithWrongReturnType")
                )
        );

        // check if existing
        matcher.registerRoute(
                new WebManager.WebRoute(
                        "test-1",
                        "/",
                        WebManager.HttpMethod.GET,
                        UtilClasses.None.class,
                        0,
                        voidHandler
                )
        );
        assertFalse(
                validator.validate(
                        "test-invalid-route-existing",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithNoParams")
                )
        );
        assertFalse(
                validator.validate(
                        "test-1",
                        "/test-invalid-name-existing",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithNoParams")
                )
        );

        // check for incompatible combinations
        assertFalse(
                validator.validate(
                        "test-invalid-get-with-body",
                        "/",
                        WebManager.HttpMethod.GET,
                        clazz.getDeclaredMethod("methodWithTwoParams", String.class, Object.class)
                )
        );

    }

}