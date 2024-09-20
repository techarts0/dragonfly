package cn.techarts.xkit.data.trans;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface Transactional {
	public boolean readonly() default false;
	public Isolation isolation() default Isolation.READ_COMMITED;
	public Propagation propagation() default Propagation.SUPPORTED;
}