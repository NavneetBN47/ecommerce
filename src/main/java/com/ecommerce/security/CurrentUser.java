package com.ecommerce.security;

import java.lang.annotation.*;

/**
 * Annotation to inject current user ID into controller methods
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}