package rithik.MockserverProject;

import java.util.concurrent.TimeUnit;

import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class MockServerSetup {
    private static ClientAndServer mockServer;

    public static void main(String[] args) {
        mockServer = ClientAndServer.startClientAndServer(1080);

        mockServer.when(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/mock")
        ).respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withBody("This is Rithik")
                .withDelay(TimeUnit.SECONDS, 2)
        );
    }
}


