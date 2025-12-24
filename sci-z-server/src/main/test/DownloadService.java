import com.sciz.server.ServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(classes = ServerApplication.class)
public class DownloadService {


    @Test
    void downloadIeeePdf2() throws Exception {
        String url = "https://ieeexplore.ieee.org/ielx7/6287639/9668973/09663184.pdf";
        Path outputPath = Paths.get("target/test-paper.pdf");
        outputPath.getParent().toFile().mkdirs();

        // ⬇️ 关键：配置更大的缓冲区（例如 10MB）
        int maxInMemorySize = 80 * 1024 * 1024; // 10 MB

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();

        WebClient client = WebClient.builder()
                .exchangeStrategies(strategies)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                .defaultHeader("Referer", "https://ieeexplore.ieee.org/")
                .build();

        byte[] pdfBytes = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .block(); // 注意：仅测试使用，生产环境避免 block()

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new RuntimeException("下载内容为空");
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            fos.write(pdfBytes);
        }

        System.out.println("✅ 文件已保存到: " + outputPath.toAbsolutePath());
        System.out.println("📄 文件大小: " + pdfBytes.length + " 字节");
    }
}