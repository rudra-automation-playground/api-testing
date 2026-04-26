import static io.restassured.RestAssured.*;
import org.testng.annotations.Test;

public class SoapAPI {

    @Test
    public void testAddOperation() {

        String requestBody =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "  <soap:Body>\n" +
                        "    <Add xmlns=\"http://tempuri.org/\">\n" +
                        "      <intA>5</intA>\n" +
                        "      <intB>10</intB>\n" +
                        "    </Add>\n" +
                        "  </soap:Body>\n" +
                        "</soap:Envelope>";

        given()
                .baseUri("http://www.dneonline.com")
                .basePath("/calculator.asmx")
                .header("Content-Type", "text/xml")
                .header("SOAPAction", "http://tempuri.org/Add")
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200)
                .log().all();
    }
}