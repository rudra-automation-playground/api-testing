import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class OASDiffUtil {

    private OASDiffUtil() {
        // Prevent instantiation
    }

    /**
     * Runs oasdiff with given command
     */
    private static OASDiffResult runCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        return new OASDiffResult(exitCode, output.toString());
    }

    /**
     * Validate breaking changes
     */
    public static void validateBreakingChanges(String oldSpec, String newSpec) {
        try {
            OASDiffResult result = runCommand(
                    "oasdiff", "breaking", oldSpec, newSpec
            );

            System.out.println("OASDIFF OUTPUT:\n" + result.getOutput());

            if (result.getExitCode() != 0) {
                throw new RuntimeException(
                        "❌ Breaking API changes detected!\n" + result.getOutput()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Error running oasdiff", e);
        }
    }

    /**
     * Get full diff (non-breaking + breaking)
     */
    public static String getDiff(String oldSpec, String newSpec) {
        try {
            OASDiffResult result = runCommand(
                    "oasdiff", "diff", oldSpec, newSpec
            );
            return result.getOutput();
        } catch (Exception e) {
            throw new RuntimeException("Error running diff", e);
        }
    }

    /**
     * Generate changelog
     */
    public static String generateChangelog(String oldSpec, String newSpec) {
        try {
            OASDiffResult result = runCommand(
                    "oasdiff", "changelog", oldSpec, newSpec
            );
            return result.getOutput();
        } catch (Exception e) {
            throw new RuntimeException("Error generating changelog", e);
        }
    }

    /**
     * Inner result class
     */
    public static class OASDiffResult {
        private final int exitCode;
        private final String output;

        public OASDiffResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }
}