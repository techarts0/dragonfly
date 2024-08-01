package cn.techarts.xkit.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebService {
	public String uri();
	public String method() default "POST";
	public boolean sessionRequired() default true;
}
