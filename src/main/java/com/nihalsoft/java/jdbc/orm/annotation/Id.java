package com.nihalsoft.java.jdbc.orm.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

//TODO : delete
@Retention(RUNTIME)
@Target({ ElementType.METHOD })
public @interface Id {

}
