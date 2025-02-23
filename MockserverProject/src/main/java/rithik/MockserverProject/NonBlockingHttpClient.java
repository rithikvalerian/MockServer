package rithik.MockserverProject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import java.util.concurrent.Future;

public class NonBlockingHttpClient {
    public static void main(String[] args) {
        try (CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault()) {
            httpAsyncClient.start();
            HttpGet request = new HttpGet("http://localhost:8080/mock");
			Future<HttpResponse> future = httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
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
			future.get(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

