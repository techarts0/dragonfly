package cn.techarts.xkit.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.inject.Qualifier;

/**
 * An identifier starts with "${" and ends with "}". <p>
 * For example: ${jdbc_datasource_driver} means that the framework 
 * needs to find the value of the key from a external configuration
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Valued {
	public String key() default "";
	public String val() default "";
}
