package armzs.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 使用http线程池
 * Created by xiangpeng on 2014/10/16f.
 */
public class HttpPoolDemo {
    public static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

    static {
        // 最大连接数
        cm.setMaxTotal(10);
        // 每个路由基础的连接数
        cm.setDefaultMaxPerRoute(10);
//        HttpHost localhost = new HttpHost("localhost", 8080);
//        cm.setMaxPerRoute(new HttpRoute(localhost), 5);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();  //获取开始时间

//        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
//            @Override
//            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
//                return 5000;
//            }
//        };
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        MyThread[] threads = new MyThread[5000];
        try {
//            IdleConnectionMonitorThread monitorThread = new IdleConnectionMonitorThread(cm);
//            monitorThread.start();
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new MyThread(httpClient, "My thread " + (i + 1));
                threads[i].start();
            }
//            Thread.sleep(20 * 1000);
//            threads[0].setShutdown();
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
            httpClient.close();

//            monitorThread.shutdown();
//            monitorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }
}

class MyThread extends Thread {
    private HttpClient client = null;

    private volatile boolean shutdown = false;

    MyThread(HttpClient client, String theadName) {
        this.client = client;
        this.setName(theadName);
    }

    @Override
    public void run() {
        HttpGet httpGet = new HttpGet("http://localhost:8080");
        HttpPost httpPost=new HttpPost("");
        try {
                HttpResponse response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int size = 0;
                    while ((size = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, size);
                    }
                    System.out.print(outputStream.toString());
                    outputStream.close();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setShutdown() {
        this.shutdown = true;
    }
}
