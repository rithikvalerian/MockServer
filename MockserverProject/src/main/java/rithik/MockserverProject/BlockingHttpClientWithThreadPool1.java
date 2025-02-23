package rithik.MockserverProject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingHttpClientWithThreadPool1 {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        int recursionLimit = new Random().nextInt(9) + 2;
        System.out.println(recursionLimit + " iterations.");
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(10);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        CountDownLatch latch = new CountDownLatch(recursionLimit); 
        try {
            recursiveHttpTask(httpClient, recursionLimit, 1, latch);
            latch.await(); 
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
            	httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            executorService.shutdown(); 
            
        }
    }

    private static void recursiveHttpTask(CloseableHttpClient httpClient, int limit, int currentIteration, CountDownLatch latch) {
        if (currentIteration > limit) {
            latch.countDown();
            System.out.println("All iterations are complete.");
            return;
        }

        executorService.submit(() -> {
            try {
            	LocalTime start=java.time.LocalTime.now();
            	HttpGet request = new HttpGet("http://localhost:8080/mock");
            	LocalTime end=java.time.LocalTime.now();
            	Duration duration = Duration.between(start, end);
            	System.out.println("Duration between start and end:"+duration.toMillis()/1000+"."+duration.toMillis()%1000+duration.toMillis()%100+duration.toMillis()%10);
                

                HttpResponse response = httpClient.execute(request);
                System.out.println("Response Code: " + response.getStatusLine().getStatusCode() + " for iteration " + currentIteration);

                String dataToEncrypt = "SensitiveDataIteration";
                String encryptedData = encryptData(dataToEncrypt);
                System.out.println("Iteration " + currentIteration + " Encrypted Data: " + encryptedData);
                recursiveHttpTask(httpClient, limit, currentIteration + 1, latch);

            } catch (Exception e) {
                System.err.println("Request failed: " + e.getMessage());
                recursiveHttpTask(httpClient, limit, currentIteration + 1, latch);
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
