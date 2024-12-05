# dragonfly: 轻量级JAVA应用开发框架

[![Generic badge](https://img.shields.io/badge/Active-00EE00.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/JAVA_11+-8A2BE2.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Restful-FFFF00.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Web-Framework-009ACD.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/GraphQL-0000CD.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Lightweight-00008B.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/ORM-009ACD.svg)](https://shields.io/)

## 介绍
Dragonfly是一个轻量级的Java应用开发框架，它基于DI框架[Whale](https://gitee.com/techarts/whale)，包括三个部分：
- A. Web: 一个遵循REST规范，参考JSR371和JAX-RS标准的Web开发框架
- B. Data:一个用统一的API集成了MyBatis,Apache DBUTILS和JPA的数据访问框架
- C. APP: 一些简化应用开发的工具
我们的初衷是：给不愿意用Spring-boot的开发者提供另外一种可靠的替代，当然也欢迎Spring的用户试一试Dragonfly。

## 使用
### 准备工作
- A. 首先，您需要新建一个Java Web项目，普通的Dynamic Web Project，或者Maven Project都可以;
- B. 因为还没有发布到MAVEN仓库中，得麻烦您手工将dragonfly-xxx.jar拷贝到您项目的WEB-INF/lib目录下，并且添加到CLASSPATH中;
- C. 在WEB-INF下或者src/main/resources下，建一个config.properties配置文件，推荐把配置文件都放在src/main/resouces目录下。
好了，下面开始写第一个Web服务。
```java
@Restful
public class BookWebService{
    @Get("/book/{id}")
    public Book getBook(WebContext arg){
        var id = arg.getInt(0);
        //Statements...
    }
}
```
有必要对上面的例子做一些解释：
- A. 注解@Restful表示此类是一个Restful风格的Web服务。如果您是Spring MVC的老用户，也可以用@Controller这个注解(JSR371)，更熟悉一些，Dragonfly都支持。取决于您如何规划URL，如果本类中所有方法的URL都具有相同的前缀，则可以在注解中给出，免得每个方法的注解上重复写：
```java
@Restful("/book")
//或者
@Controller("/book")
```
- B. Web方法的返回值不限，取决于您的业务。为了简化，Dragonfly采用了一个强制约束：每个方法都只有一个参数，类型为WebContext，WebContext中有您需要的一切，比如获取请求参数或设置错误代码/信息等。这种设计是为了让代码更简洁，形式更统一。
- C. Web方法上的注解，表示它是哪一类REST请求（HTTP METHOD），以及资源的URL路径和参数。URL设计是个难点，需要根据业务去自习琢磨。Dragonfly支持以下HTTP METHODS:
| ID| HTTP Method | 注解      | 用途            |
|---|-------------|---------|---------------|
| 1 | GET         | @Get    | 查询资源          |
| 2 | PUT         | @Put    | 修改资源          |
| 3 | POST        | @Post   | 创建资源          |
| 4 | DELETE      | @Delete | 删除资源          |
| 5 | HEAD        | @Head   |               |
| 6 | PATCH       | @Patch  |               |
| 7 | 不区分方法       | @Any    | 兼容非REST风格的API |
