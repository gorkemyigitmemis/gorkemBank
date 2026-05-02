import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

public class FixFormat {
    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/templates");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".html"));
        if (files == null) return;
        
        Pattern pattern = Pattern.compile("'COMMA',\\s*(\\d+),\\s*'POINT'");
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Matcher matcher = pattern.matcher(content);
            String newContent = matcher.replaceAll("'POINT', $1, 'COMMA'");
            if (!content.equals(newContent)) {
                Files.write(file.toPath(), newContent.getBytes(StandardCharsets.UTF_8));
                System.out.println("Updated " + file.getName());
            }
        }
    }
}
