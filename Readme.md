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
