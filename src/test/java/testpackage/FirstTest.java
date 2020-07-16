package testpackage;

import com.typesafe.config.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;

import org.json.JSONException;
import org.json.JSONObject;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
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

        cookie = getCookie();


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
                .cookie(cookieName, cookie)
                .body("{\"login\": \"entera_test@mail.ru\"}")
                .post(authURL + "/api/v1/impersonate")
                .then().body("result", equalTo(true));

    }

    @Test(dataProvider = "dataSet")
    public void getDocument(String id, Boolean result){
        given().cookie(cookieName, cookie)
                .get(frontURL + "/api/v1/documents/" + id)
                .then().body("result", equalTo(result)).log().ifValidationFails();

    }

    @Test(dataProvider = "dataSetForUploadFiles")
    public void uploadFile(String spaceId, String filePath, Boolean result){
        given()
                .cookie(cookieName, cookie)
                .multiPart(new File(filePath))
                .post(frontURL + "/api/v1/recognitionTasks?spaceId=" + spaceId)
                .then().body("result", equalTo(result));

    }

    //Проверка схемы json
    @Test
    public void validationSchema(){
        JSONObject json = new JSONObject();
        json.put("login", "");
        json.put("password", "");

        Response response=  given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post("https://id-test2.hr-link.ru/api/v1/login");

        given()
                .cookie("sessionId", response.getCookie("sessionId"))
                .get("https://app-test2.hr-link.ru/api/v1/clients/0064ed51-8896-4008-84b3-4f03a12687cb/departments/123/externalId")
                .then()
                .body(matchesJsonSchemaInClasspath("validschema.json"));
    }

    @BeforeTest
    public void filter(){
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    private String getCookie(){

        JSONObject json = new JSONObject();
        json.put("login", baseLogin);
        json.put("password", basePassword);

        Response response=  given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .post(authURL + "/api/v1/login");
        return response.getCookie(cookieName);
    }

    @DataProvider(name = "dataSet")
    public Object[][] createData() {

        return new Object[][] {
                {"567fc18e-60aa-4ded-b328-8a665af8b0fa", true},
                {"36ee9177-6f1f-4239-a649-8f0fe9eca458", true},
                {"invalidValue", true}
        };
    }

    @DataProvider(name = "dataSetForUploadFiles")
    public Object[][] createDataSetForUploadFiles() {

        return new Object[][] {
                {"4be4e911-e233-42df-80ec-c5154c6605c4", "C:\\Users\\mage\\Desktop\\testfile.png", true},
        };
    }


}
