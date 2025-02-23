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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NonBlockingHttpClientWithThreadPool {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10); 
        try (CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault()){
            httpAsyncClient.start();
            HttpGet request = new HttpGet("http://localhost:8080/mock");
            
            Future<HttpResponse> future = httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse response) {
                    System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
                    
                    executorService.submit(() -> {
                        try {
                            String dataToEncrypt = "SensitiveData";
                            String encryptedData = encryptData(dataToEncrypt);
                            System.out.println("Encrypted Data: " + encryptedData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failed(Exception ex) {
                    System.err.println("Request failed: " + ex.getMessage());
                }

                @Override
                public void cancelled() {
                    System.out.println("Request cancelled");
                }
            });

            future.get();
            } 
            catch (Exception e) {
            e.printStackTrace();
            } 
            finally {
            executorService.shutdown();
            }
        
       
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
