package com.ecommerce.security;

import java.lang.annotation.*;

/**
 * Annotation to inject current user ID from security context
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}