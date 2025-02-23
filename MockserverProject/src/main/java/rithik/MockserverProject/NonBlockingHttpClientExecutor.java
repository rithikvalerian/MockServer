package rithik.MockserverProject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NonBlockingHttpClientExecutor {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        try (CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault()) {
            httpAsyncClient.start();
            for (int i = 0; i < 100; i++) {
                executorService.submit(() -> {
                    HttpGet request = new HttpGet("http://localhost:1080/mock");
                    httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
                        @Override
                        public void completed(HttpResponse response) {
                            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
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
                });
            }
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

