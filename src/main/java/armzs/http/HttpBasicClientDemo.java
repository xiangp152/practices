package armzs.http;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiangpeng on 2014/10/15.
 */
public class HttpBasicClientDemo {

    public static void testBasicConnnection() {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = HttpClients.createDefault();
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("hb.sina.com.cn")
                    .setPath("/news/j/2014-10-16/detail-ianfzhne6428342.shtml")
                    .setPort(80)
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            //revieve response
            response = client.execute(httpGet);
            System.out.println(response.getProtocolVersion());
            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println(response.getStatusLine().getReasonPhrase());
            System.out.println(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedOutputStream outputStream = new BufferedOutputStream(System.out);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = instream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, size);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();  //获取开始时间
        PerConnThread[] perConnThread = new PerConnThread[5000];
        for (int i = 0; i < perConnThread.length; i++) {
            perConnThread[i] = new PerConnThread();
        }
        for (int i = 0; i < perConnThread.length; i++) {
            perConnThread[i].start();
        }

        for (int i = 0; i <perConnThread.length; i++) {
            perConnThread[i].join();
        }

        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }
}

class PerConnThread extends Thread {
    @Override
    public void run() {
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) new URL("http://localhost:8080").openConnection();
            httpConn.setConnectTimeout(1000*60);
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            InputStream inputStream = httpConn.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            System.out.print(outputStream.toString());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }
}
