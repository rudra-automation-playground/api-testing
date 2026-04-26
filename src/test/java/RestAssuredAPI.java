import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.response.Response;
import org.apache.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;

@Listeners({AllureTestNg.class})
public class RestAssuredAPI {
    @Test(groups = {"flaky"})
    @Parameters("baseURL")
    public void hybridValidationTest(String baseURL) throws Exception {

        RestAssured.baseURI = baseURL;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data =
                mapper.readValue(new File("src/test/resources/pet.json"), Map.class);


        Response response = RestAssured
                .given()
                .config(RestAssured.config().
                        httpClient(HttpClientConfig.httpClientConfig().
                                setParam("http.protocol.version", HttpVersion.HTTP_1_1)))
                .log().all()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(data)
                .when()
                .post("/pet")
                .then()
                .time(lessThan(20000L)) // response time < 2 seconds
                .log().all()
                .extract()
                .response();
        // 🔹 Step 2: Extract API values
        int apiId = response.jsonPath().getInt("id");
        String apiName = response.jsonPath().getString("name");
        String apiStatus = response.jsonPath().getString("status");
        System.out.println(response.getBody().asString());
        System.out.println("Status Line: " + response.getStatusLine());
        int size = response.getBody().asString().length();
        System.out.println("Response Size: " + size);
        // Assert.assertTrue(size < 5000); // example validation

        String url = "https://petstore.swagger.io/v2/pet";
        URI uri = new URI(url);
        String host = uri.getHost();
        InetAddress address = InetAddress.getByName(host);
        System.out.println("Host: " + host);
        System.out.println("Local Host Name: " + address.getHostName());
        System.out.println("Remote IP: " + address.getHostAddress());

        List<String> expectedStatuses = Arrays.asList("available", "pending", "sold");
        List<String> actualStatuses = response.jsonPath().getList("data.status");
      //  Assert.assertTrue(actualStatuses.size() >= 2, "Not enough elements in actual list");
//        Assert.assertEquals(actualStatuses.subList(0, 2), expectedStatuses);

//        // 🔹 Step 3: DB Connection
//        Connection con = DriverManager.getConnection(
//                "jdbc:mysql://localhost:3306/petstore",
//                "root",
//                "password"
//        );
//
//        Statement stmt = con.createStatement();
//
//        // 🔥 Step 4: Execute YOUR query
//        String query = "SELECT p.id, p.name, p.status, c.name AS category_name, " +
//                "GROUP_CONCAT(t.name) AS tags " +
//                "FROM pet p " +
//                "LEFT JOIN category c ON p.category_id = c.id " +
//                "LEFT JOIN pet_tag pt ON p.id = pt.pet_id " +
//                "LEFT JOIN tag t ON pt.tag_id = t.id " +
//                "WHERE p.id = " + apiId + " GROUP BY p.id";
//
//        ResultSet rs = stmt.executeQuery(query);
//
//        // 🔹 Step 5: Validate DB vs API
//        if (rs.next()) {
//            int dbId = rs.getInt("id");
//            String dbName = rs.getString("name");
//            String dbStatus = rs.getString("status");
//            String dbCategory = rs.getString("category_name");
//            String dbTags = rs.getString("tags");
//
//            // Assertions
//            assertEquals(apiId, dbId);
//            assertEquals(apiName, dbName);
//            assertEquals(apiStatus, dbStatus);
//
//            System.out.println("✅ DB Validation Successful");
//            System.out.println("Category: " + dbCategory);
//            System.out.println("Tags: " + dbTags);
//        } else {
//            System.out.println("❌ No data found in DB");
//        }
//
//        // 🔹 Step 6: Close connection
//        rs.close();
//        stmt.close();
//        con.close();

    }
}