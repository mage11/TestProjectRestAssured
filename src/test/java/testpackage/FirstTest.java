package testpackage;

import com.typesafe.config.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.json.JSONException;
import org.json.JSONObject;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;



public class FirstTest {

    private static String baseLogin;
    private static String basePassword;
    private static String frontURL;
    private static String authURL;
    private static String cookie;
    private static String cookieName;

    //Выполняется перед тестом. Получаем необходимые переменные из test.conf
    @BeforeTest
    public void setUp(){

        Config conf = ConfigFactory.load("test");

        baseLogin = conf.getString("baseLogin");
        basePassword = conf.getString("basePassword");
        frontURL = conf.getString("front");
        authURL = conf.getString("auth");
        cookieName = conf.getString("cookieName");

        cookie = getCoockie();


    }

    //Проверка версии
    @Test()
    public void getRequestExampleTest() throws JSONException {

        String ver = "0.21.0\n";

        given()
                .get(authURL + "/api/version")
                .then().assertThat().contentType(ContentType.TEXT)
                .assertThat().body(equalTo(ver));
    }

    //Проверка /impersonate
    @Test
    public void login() {

        given().contentType(ContentType.JSON)
                .cookie("ENTERA_JWT_TEST", cookie)
                .body("{\"login\": \"entera_test@mail.ru\"}")
                .post(authURL + "/api/v1/impersonate")
                .then().body("result", equalTo(true));

    }

    private String getCoockie(){

        JSONObject json = new JSONObject();
        json.put("login", baseLogin);
        json.put("password", basePassword);

        Response response=  given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(authURL + "/api/v1/login");
        return response.getCookie(cookieName);
    }

}
