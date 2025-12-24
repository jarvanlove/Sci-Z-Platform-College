import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfDownloader {

    public static void downloadPdf(String pdfUrl, String destinationFile) {
        try {
            URL url = new URL(pdfUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(true);

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 status code
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
                inputStream.close();
                System.out.println("PDF downloaded successfully: " + destinationFile);
            } else {
                System.out.println("Failed to download PDF, response code: " + responseCode);
            }
            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String pdfUrl = "https://www.sciencedirect.com/science/article/am/pii/S0167865520302075?via=ihub";
        String destinationFile = "D:/"; // 目标文件名
        downloadPdf(pdfUrl, destinationFile);
    }
}