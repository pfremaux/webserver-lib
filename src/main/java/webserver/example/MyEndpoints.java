package webserver.example;

import java.util.List;
import java.util.Map;

import tools.MdDoc;
import webserver.annotations.Endpoint;
import webserver.annotations.Role;
import webserver.annotations.validator.Validator;

public class MyEndpoints {

	@Endpoint(path = "/toto", method = "GET")
	@MdDoc(description = "My description!!!")
	// TODO PFR remettre @Role(value = "coucou")
	public Body go(Map<String, List<String>> headers, Body body) {
		return new Body("reponse");
	}

	@Endpoint(path = "/totoPost", method = "POST")
	public Body post(Map<String, List<String>> headers, Body body) {
		return new Body("reponse");
	}

	@Endpoint(path = "/sum", method = "POST")
	public SumResult sum(Map<String, List<String>> headers, Pair body) {
		return new SumResult(body.getA() + body.getB());
	}

	@Endpoint(path = "/testArray", method = "POST")
	public TestArray array(Map<String, List<String>> headers, TestArray2 body) {
		return new TestArray(List.of("aaa", "bbb"), List.of(3.1, 2.2), List.of(new Body("toto1"), new Body("toto2")));
	}

}
