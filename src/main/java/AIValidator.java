public class AIValidator {

    public static String getValidationResponse(String json, String rule) {
        String prompt = """
        Validate the JSON and respond in this format:

        PASSED:
        - <list of passed validations>

        FAILED:
        - <list of failed validations>

        JSON:
        """ + json + "\nRules:\n" + rule;

        // 🔹 Replace with real LLM call
        return callLLM(prompt);
    }

    public static boolean validate(String json, String rule) {
        String response = getValidationResponse(json, rule);
        return response.toLowerCase().contains("failed:") == false;
    }

    private static String callLLM(String prompt) {
        // 🔹 Mock response (replace later)
        return """
        PASSED:
        - id is valid
        - name is not empty
        - status is valid
        - category.id is valid
        - category.name is valid
        """;
    }
}