package testpackage;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.json.JSONException;
import org.json.JSONObject;

import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;



public class FirstTest {

    private static String login = "XXX";
    private static String password = "XXX";

    @Test()
    public void getRequestExampleTest() throws JSONException {

        String ver = "0.21.0\n";

        given()
                .get("http://id-test2.entera.pro/api/version")
                .then().assertThat().contentType(ContentType.TEXT)
                .assertThat().body(equalTo(ver));
    }

    @Test
    public void login() {

        String jwt = getCoockie();


        given().contentType(ContentType.JSON)
                .cookie("ENTERA_JWT_TEST", jwt)
                .body("{\"login\": \"entera_test@mail.ru\"}")
                .post("https://id-test2.entera.pro/api/v1/impersonate")
                .then().body("result", equalTo(true));

    }

    private String getCoockie(){
        String URL = "https://id-test2.entera.pro/api/v1/login";

        JSONObject json = new JSONObject();
        json.put("login", login);
        json.put("password", password);

        Response response=  given()
                .header("Content-Type","application/json")
                .body(json.toString())
                .post(URL);
        return response.getCookie("ENTERA_JWT_TEST");
    }

}
