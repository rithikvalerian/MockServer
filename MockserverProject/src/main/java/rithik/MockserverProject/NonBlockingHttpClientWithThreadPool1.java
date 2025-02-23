package rithik.MockserverProject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingHttpClientWithThreadPool1 {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        int recursionLimit = new Random().nextInt(9) + 2;
        System.out.println(recursionLimit + " iterations.");

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault();
        httpAsyncClient.start();
        recursiveHttpTask(httpAsyncClient, recursionLimit, 1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                httpAsyncClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            executorService.shutdown();
        }));
    }

    private static void recursiveHttpTask(CloseableHttpAsyncClient httpAsyncClient, int limit, int currentIteration) {
        if (currentIteration > limit) {
            System.out.println("All iterations completed.");
            return;
        }

        HttpGet request = new HttpGet("http://localhost:8080/mock");
        httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                System.out.println("Response Code: " + response.getStatusLine().getStatusCode() + " for iteration " + currentIteration);
                
                executorService.submit(() -> {
                    try {
                        String dataToEncrypt = "SensitiveDataIteration";
                        String encryptedData = encryptData(dataToEncrypt);
                        System.out.println("Iteration " + currentIteration + " Encrypted Data: " + encryptedData);

                        recursiveHttpTask(httpAsyncClient, limit, currentIteration + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void failed(Exception ex) {
                System.err.println("Request failed: " + ex.getMessage());
                recursiveHttpTask(httpAsyncClient, limit, currentIteration + 1);
            }

            @Override
            public void cancelled() {
                System.out.println("Request cancelled for iteration " + currentIteration);
                recursiveHttpTask(httpAsyncClient, limit, currentIteration + 1);
            }
        });
    }

    public static String encryptData(String data) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);  
        SecretKey secretKey = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
