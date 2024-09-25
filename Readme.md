# dragonfly-ioc

## Summary
dragonfly-ioc is a sub-project of project dragonfly. It's a lightweight IOC Container that fulfills JSR330.

## Usage
dragonfly-ioc supports 3 ways to define the dependence of java classes.
### JSR 330 Annotation

```
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
    private static Map<String, String> configs = Map.of("user.name", "John", "mobile.area", "+86", "mobile.number", "13666666666");
    @Test
    public void testInject(){
        var context = Context.make(configs);
        context.createFactory().register(Person.class).register(Mobile.class).start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
}
```

