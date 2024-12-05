# dragonfly: 轻量级JAVA应用开发框架

[![Generic badge](https://img.shields.io/badge/Active-00EE00.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/JAVA_11+-8A2BE2.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Restful-FFFF00.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/MVC-009ACD.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/GraphQL-0000CD.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Lightweight-00008B.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/ORM-009ACD.svg)](https://shields.io/)

## 1. 介绍
Dragonfly是一个轻量级的Java应用开发框架，它基于DI框架[Whale](https://gitee.com/techarts/whale)，包括三个部分：
- Web: 一个遵循REST规范，参考JSR371和JAX-RS标准的Web开发框架
- Data:一个用统一的API集成了MyBatis,Apache DBUTILS和JPA的数据访问框架
- APP: 一些简化应用开发的工具

我们的初衷是：给不愿意用Spring-boot的开发者提供另外一种可靠的替代，当然也欢迎Spring的用户试一试Dragonfly。

### 1.1 准备工作
- 首先，您需要新建一个Java Web项目，普通的Dynamic Web Project，或者Maven Project都可以;
- 因为还没有发布到MAVEN仓库中，得麻烦您手工将dragonfly-xxx.jar拷贝到您项目的WEB-INF/lib目录下，并且添加到CLASSPATH中;
- 在WEB-INF下或者src/main/resources下，建一个config.properties配置文件，推荐把配置文件都放在src/main/resouces目录下。config.properties里空着没关系，后面我们逐步往里面增加内容。

### 2. Web Framework
我们先写一个简单的Web服务。

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
这个例子虽然简单，但它几乎包含了Dragonfly的全部。

#### 2.1 注解@Restful
表示此类是一个Restful风格的Web服务。如果您是Spring MVC的老用户，也可以用@Controller这个注解(JSR371)，更熟悉一些，Dragonfly都支持。取决于您如何规划URL，如果本类中所有方法的URL都具有相同的前缀，则可以在注解中给出，免得每个方法的注解上重复写：
```java
@Restful("/book")
//或者
@Controller("/book")
```
#### 2.2 Web方法形式
的返回值不限，取决于您的业务。为了简化，Dragonfly采用了一个强制约束：每个方法都只有一个参数，类型为WebContext，WebContext中有您需要的一切，比如获取请求参数或设置错误代码/信息等。这种设计是为了让代码更简洁，形式更统一。

#### 2.3 Web方法注解
注解表示它是哪一类REST请求（HTTP METHOD），以及资源的URL路径和参数。URL设计是个难点，需要根据业务去仔细琢磨。Dragonfly支持以下HTTP METHODS:


| # | HTTP Method | 注解      | 用途            |
|---|-------------|---------|---------------|
| 1 | GET         | @Get    | 查询资源          |
| 2 | PUT         | @Put    | 修改资源          |
| 3 | POST        | @Post   | 创建资源          |
| 4 | DELETE      | @Delete | 删除资源          |
| 5 | HEAD        | @Head   |               |
| 6 | PATCH       | @Patch  |               |
| 7 | 不区分方法   | @Any    | 兼容非REST风格的API |


在一个业务稍复杂的应用中，REST的严格约束会让开发者感到很不方便。因此，Dragonfly提供了一个@Any注解，兼容传统设计风格的Web API。但是需要注意：它的URL中不能使用路径参数了，也就是说，所有参数要么通过POST form传过来，要么通过Query String传过来。

#### 2.4 获取请求参数
每个注解的value属性中，如果含有参数，需要放在一对花括号{}中。在方法体内，获取Request参数有两种方式：
- A. 如果是URL路径中花括号里的参数，需要用它的位置索引，从0开始，依次增加。这是一种设计权衡，为了避免Spring MVC和JAX-RS中的@PathVariable和@PathParam注解对参数形式的破坏，这两个东西看起来乱糟糟的。WebContext内置了多个方法获取不同类型的参数，包括：

| # | 方法       | 参数  | 返回值     |
|---|----------|-----|---------|
| 1 | get      | int | String  |
| 2 | getInt   | int | int     |
| 3 | getFloat | int | float   |
| 4 | getLong  | int | long    |
| 5 | getBool  | int | boolean |

当然，您也可以只是用get(int)，将获得的字符串使用Dragonfly提供的Converter工具进行类型转换。

- B. 如果是通过POST表单(form)或者GET QueryString(?xx=xxx)传过来的参数，需要根据参数的名称获取。WebContext中提供了丰富的方法：

| # | 方法        | 参数     | 返回值类型       |
|---|-----------|--------|-------------|
| 1 | get       | String | String      |
| 2 | getInt    | String | int         |
| 3 | getFloat  | String | float       |
| 4 | getLong   | String | long        |
| 5 | getBool   | String | boolean     |
| 6 | getDate   | String | Date        |
| 7 | getJson   | 无      | JSON String |
| 8 | bean      | T      | T           |
| 9 | getDouble | String | double      |


#### 2.5 错误处理
如果要在Response中向调用者传递错误信息，可以使用WebContext的error方法：

```java
@Get("/book/{id}")
public Book getBook(WebContext arg){
    arg.error(-2, "The book does not exist");
}
```

#### 2.6 响应数据格式
Dragonfly将以JSON格式返回数据给请求者，并且模式是固定的：
```java
public class Result implements Serializable{
    private int code;      //错误代码，0表示正确
    private String text;   //错误描述，0对应为OK
    private Object data;   //业务数据
}
```

它的JSON格式看起来是这样的：
```json
{
  "code": 0,
  "text": "OK",
  "data": {
    "id": 166,
    "isbn": "222-3F",
    "name": "枯枝败叶",
    "author": "马尔克斯"
  }
}
```

#### 2.7 其它功能
WebContext中还有更丰富的内容，包括以下几类：
- 使用getRequest()原生的HttpServletRequest, getResponse()获取原生的HttpServletResponse
- 对于一些常见的参数名称，比如id, name, date, time, page做了特殊处理，直接用id(), name(), date(), time(), page()等简洁方法就可以获取到值
- head(String)可以获取HTTP请求头字段的信息，ip()可以获取客户端的IP地址，ua()获取客户端的User-Agent，等等。