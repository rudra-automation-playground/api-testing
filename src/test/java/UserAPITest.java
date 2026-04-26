import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class UserAPITest{

    private static final String TARGET_DIR = "target";
    private static final String RESPONSE_PREFIX = "response_";
    private static final String AI_PREFIX = "ai_validation_";

    private final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    @BeforeSuite
    public void validateAPIContract() {
        OASDiffUtil.validateBreakingChanges(
                "src/test/resources/old.yaml",
                "src/test/resources/new.yaml"
        );}
    // 🔹 Run once before class
    @BeforeClass
    public void setup() throws Exception {
        createTargetDirectory();
        cleanOldFiles();
    }

    // 🔹 Main Test
//   @Test(groups = {"flaky"})
  //  @Parameters("baseURL")
    public void hybridValidationTest(String baseURL) throws Exception {
        RestAssured.baseURI = baseURL;
        Map<String, Object> requestData = loadRequest("src/test/resources/pet.json");
        String response = executePost("/pet", requestData);
        String timestamp = getTimestamp();
        Path responseFile = getFilePath(RESPONSE_PREFIX, timestamp, ".json");
        Path aiReportFile = getFilePath(AI_PREFIX, timestamp, ".txt");
        writeToFile(responseFile, response);
        String rule = getValidationRule();
        String aiResponse = AIValidator.getValidationResponse(response, rule);
        writeToFile(aiReportFile, aiResponse);
        printValidationResult(aiResponse);
        validateFinalResult(aiResponse, responseFile, aiReportFile);
    }

    // ================= UTIL METHODS =================

    private Map<String, Object> loadRequest(String filePath) throws Exception {
        return mapper.readValue(new File(filePath), Map.class);
    }

    private String executePost(String endpoint, Map<String, Object> body) {
        return given()
             //   .filter(new OpenApiValidationFilter("src/test/resources/new.yaml"))
                .log().all()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .log().all()
                .statusCode(200)
            //    .body(matchesJsonSchemaInClasspath("src/test/resources/schemas/userSchema.json")) // Extra safety
                .extract()
                .asPrettyString();
    }

    private void createTargetDirectory() throws Exception {
        Files.createDirectories(Path.of(TARGET_DIR));
    }

    private void cleanOldFiles() throws Exception {
        try (Stream<Path> paths = Files.list(Path.of(TARGET_DIR))) {
            paths.filter(this::isGeneratedFile)
                    .forEach(this::deleteFile);
        }
    }

    private boolean isGeneratedFile(Path path) {
        String name = path.getFileName().toString();
        return name.startsWith(RESPONSE_PREFIX) || name.startsWith(AI_PREFIX);
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
            System.out.println("🗑 Deleted: " + path);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to delete: " + path);
        }
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(formatter);
    }

    private Path getFilePath(String prefix, String timestamp, String extension) {
        return Path.of(TARGET_DIR, prefix + timestamp + extension);
    }

    private void writeToFile(Path path, String content) throws Exception {
        Files.writeString(path, content);
        System.out.println("📄 Saved: " + path);
    }

    private String getValidationRule() {
        return """
        Validate the JSON request body:

        - id must be non-negative integer
        - name must not be empty
        - status must be available/pending/sold
        - category.id must be non-negative
        - category.name must not be empty
        - tags must have id and name if present

        Return output in this format:

        PASSED:
        - ...

        FAILED:
        - ...
        """;
    }

    private void printValidationResult(String aiResponse) {
        System.out.println("\n====== AI VALIDATION RESULT ======");
        System.out.println(aiResponse);

        String[] split = aiResponse.split("FAILED:", 2);
        String passed = split[0];
        String failed = split.length > 1 ? split[1] : "";

        System.out.println("\n====== PASSED VALIDATIONS ======");
        Arrays.stream(passed.split("\n"))
                .filter(line -> line.trim().startsWith("-"))
                .forEach(line -> System.out.println("✅ " + clean(line)));

        if (!failed.isBlank()) {
            System.out.println("\n====== FAILED VALIDATIONS ======");
            Arrays.stream(failed.split("\n"))
                    .filter(line -> line.trim().startsWith("-"))
                    .forEach(line -> System.out.println("❌ " + clean(line)));
        }
    }

    private String clean(String line) {
        return line.replace("-", "").trim();
    }

    private void validateFinalResult(String aiResponse, Path responseFile, Path aiFile) {
        boolean isValid = !aiResponse.contains("FAILED:");

        Assert.assertTrue(isValid,
                "\n❌ AI Validation Failed" +
                        "\n📄 Response File: " + responseFile +
                        "\n📊 AI Report: " + aiFile +
                        "\n\nDetails:\n" + aiResponse);
    }
}