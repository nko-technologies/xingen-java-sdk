package de.xingen.sdk.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartBodyPublisherTest {

    @Test
    void encodesFilePartWithBoundaryHeadersAndContent() throws InterruptedException {
        MultipartBodyPublisher multipart = new MultipartBodyPublisher()
            .addFilePart("file", "invoice.xml", "<Invoice/>".getBytes(StandardCharsets.UTF_8), "application/xml");

        String body = drain(multipart.build());
        String boundary = extractBoundary(multipart.contentTypeHeader());

        assertThat(body).startsWith("--" + boundary + "\r\n");
        assertThat(body).contains("Content-Disposition: form-data; name=\"file\"; filename=\"invoice.xml\"\r\n");
        assertThat(body).contains("Content-Type: application/xml\r\n");
        assertThat(body).contains("\r\n\r\n<Invoice/>\r\n");
        assertThat(body).endsWith("--" + boundary + "--\r\n");
    }

    @Test
    void encodesFormFieldWithoutFilenameOrContentType() throws InterruptedException {
        MultipartBodyPublisher multipart = new MultipartBodyPublisher().addFormField("name", "value");

        String body = drain(multipart.build());

        assertThat(body).contains("Content-Disposition: form-data; name=\"name\"\r\n\r\nvalue\r\n");
        assertThat(body).doesNotContain("filename=").doesNotContain("Content-Type:");
    }

    @Test
    void supportsMultiplePartsInOneBody() throws InterruptedException {
        MultipartBodyPublisher multipart = new MultipartBodyPublisher()
            .addFilePart("file", "invoice.xml", "<a/>".getBytes(StandardCharsets.UTF_8), "application/xml");
        String boundary = extractBoundary(multipart.contentTypeHeader());

        String body = drain(multipart.build());

        // Exactly one opening occurrence of the boundary as a part separator, plus the closing "--boundary--".
        assertThat(body.split("--" + boundary + "\r\n", -1)).hasSize(2);
    }

    private static String extractBoundary(String contentTypeHeader) {
        return contentTypeHeader.substring(contentTypeHeader.indexOf("boundary=") + "boundary=".length());
    }

    private static String drain(HttpRequest.BodyPublisher publisher) throws InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CountDownLatch done = new CountDownLatch(1);
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                out.writeBytes(bytes);
            }

            @Override
            public void onError(Throwable throwable) {
                done.countDown();
            }

            @Override
            public void onComplete() {
                done.countDown();
            }
        });
        done.await();
        return out.toString(StandardCharsets.UTF_8);
    }
}
