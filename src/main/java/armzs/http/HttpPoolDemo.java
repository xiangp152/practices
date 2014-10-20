package armzs.http;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
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
        cm.setMaxTotal(2);
        // 每个路由基础的连接数
        cm.setDefaultMaxPerRoute(1);
//        HttpHost localhost = new HttpHost("localhost", 8080);
//        cm.setMaxPerRoute(new HttpRoute(localhost), 5);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();  //获取开始时间
//        testPool();
        try {
            testBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }

    /**
     * 测试对多个服务器建立连接时候的阻塞情况
     */
    private static void testBlocking() throws InterruptedException, IOException {
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

            public boolean retryRequest(
                    IOException exception,
                    int executionCount,
                    HttpContext context) {
                if (executionCount >= 6) {
                    // 如果已经重试了5次，就放弃
                    return false;
                }
//                if (exception instanceof InterruptedIOException) {
//                    // 超时
//                    return false;
//                }
//                if (exception instanceof UnknownHostException) {
//                    // 目标服务器不可达
//                    return false;
//                }
//                if (exception instanceof ConnectTimeoutException) {
//                    // 连接被拒绝
//                    return false;
//                }
//                if (exception instanceof SSLException) {
//                    // ssl握手异常
//                    return false;
//                }
//                HttpClientContext clientContext = HttpClientContext.adapt(context);
//                HttpRequest request = clientContext.getRequest();
//                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
//                if (idempotent) {
//                    // 如果请求是幂等的，就再次尝试
//                    return true;
//                }
                return true;
            }

        };
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(cm)
//                .setRetryHandler(myRetryHandler)
                .build();
        BlockThread thread1 = new BlockThread(client, "Thread1", "http://localhost:8080");
        BlockThread thread2 = new BlockThread(client, "Thread2", "http://localhost:8080");
        thread1.start();
        thread1.join();
//        Thread.sleep(15000);
//        thread2.start();
//        thread2.join();
        client.close();
    }


    /**
     * 测试连接池的连接复用情况
     */
    public static void testPool() {

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
        HttpPost httpPost = new HttpPost("");

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

class BlockThread extends Thread {
    private final String target;
    private HttpClient client = null;

    private volatile boolean shutdown = false;

    BlockThread(HttpClient client, String theadName, String target) {
        this.client = client;
        this.setName(theadName);
        this.target = target;
    }

    @Override
    public void run() {
        HttpPost httpPost = new HttpPost(target);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(2000)
                .setSocketTimeout(2000)
                .setConnectTimeout(2000)
                .build();//设置请求和传输超时时间

        httpPost.setConfig(requestConfig);
        httpPost.setConfig(requestConfig);
        try {
            HttpEntity sendEntity = new FileEntity(new File("E:\\Downloads\\ut.rar"));
            httpPost.setEntity(sendEntity);
            System.out.println(this.getName() + " start sending data");
            HttpResponse response = null;
            response = client.execute(httpPost);
            System.out.println(this.getName() + " send end");
            HttpEntity getEntity = response.getEntity();
            if (getEntity != null) {
                InputStream inputStream = getEntity.getContent();
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
