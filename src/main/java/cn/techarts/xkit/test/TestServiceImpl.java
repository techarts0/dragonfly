package cn.techarts.xkit.test;

public class TestServiceImpl implements TestService {
	public String sayHello(String person) {
		//"".substring(0, 3);
		return "Hello ".concat(person);
	}
}
