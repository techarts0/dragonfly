# dragonfly-ioc

## Summary
dragonfly-ioc is a sub-project of project dragonfly. It's a lightweight IOC Container that fulfills JSR330.

## Basic Usage
dragonfly-ioc supports 3 ways to define the dependence of java classes.
### 1. JSR 330 Annotation

```
package ioc.demo;

@Named
@Singleton
public class Person{
    @Inject 
    @Valued(val="3")
    private int id;
    
    @Inject
    @Valued(key="user.name") 
    private String name;
    
    @Inject
    private Mobile mobile;
    
    public Person(){}

    //Getters and Setters
}

package ioc.demo;

@Singleton
public class Mobile{
    private String areaCode;
    private String number;

    @Inject
    public Mobile(@Valued(key="mobile.area")String areaCode, @Valued(key="mobile.number")String number){
        this.areaCode = areaCode;
        this.number = number;
    }
    //Getters & Setters
}

public class JSR330Test{
    private static Map<String, String> configs = Map.of("user.name", "John", 
                                                        "mobile.area", "+86", 
                                                        "mobile.number", "13666666666");
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.register(Person.class).register(Mobile.class);
        //factory.register("ioc.demo.person").register("ioc.demo.Mobile");
        //factory.register(Person.class, Mobile.class);
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
}
```
We suppose the above 2 classes is under the folder "/usr/project/demo/classes", the framework will scan the classpath to register beans:

```
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.scan("/usr/project/demo/classes");
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
```
Maybe, the classes in the JAR file demo.jar and it's full path is "/usr/project/demo/lib/demo.jar":

```
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.load("/usr/project/demo/lib/demo.jar");
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
```
### 2. XML Definition(beans.xml)

If you are a spring-framework developer, the XML configuration is very familiar. dragonfly-ioc allows you define the manged beans in the XML file(beans.xml):
```
`<beans>
    <bean id="person" singleton="true" type="ico.demo.Person">
        <props>
	    <prop name="id" val="45" />
	    <prop name="name" key="user.name" />
	</props>
    </bean>
    <bean id="mobile" singleton="true" type="ico.demo.Mobile">
        <args>
	    <arg key="mobile.area" type="String" />
	    <arg key="mobile.number" type="String" />
	</args>
    </bean>
</beans>` 
```
Please note, XML does not support method rejection, because the grammar is to complex and urgly :(
```
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.parse("/usr/project/demo/classes/beans.xml");
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
```
Actually, you can put multiple xml definitions to the method parse like the following:
```
    factory.parse("/usr/project/demo/classes/beans_1.xml", "/usr/project/demo/classes/beans_2.xml");
```