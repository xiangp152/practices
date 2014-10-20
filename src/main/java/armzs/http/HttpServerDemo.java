package armzs.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpException;

import javax.xml.ws.http.HTTPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用来测试的http服务器
 * Created by xiangpeng on 2014/10/16.
 */
public class HttpServerDemo {

    public static int threadMinCount = 3;//最小线程数
    public static int threadMaxCount = 5;//最大线程数
    public static int checkPeriod = 20;//检验时间间隔(分钟)
    public static int listenPort = 8080;//检验时间间隔(分钟)


    public static void main(String[] args) {

        InetSocketAddress addr = new InetSocketAddress(listenPort);
        try {
            HttpServer server = HttpServer.create(addr, 0);
            System.out.println("Server starting ...");
            server.createContext("/", new MyHandler());
            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(threadMinCount, threadMaxCount, checkPeriod,
                    TimeUnit.MINUTES, new ArrayBlockingQueue(6),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            server.setExecutor(threadPool);
            server.start();
            System.out.println("Listening at " + listenPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MyHandler implements HttpHandler {
    private static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        // 处理请求
        int sequence = count.incrementAndGet();
        System.out.println(sequence);
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0);

//        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        InputStream requestBody = exchange.getRequestBody();
        byte[] buff = new byte[1024];
        int size = 0;
        try {
            while ((size = requestBody.read(buff)) != -1) {
//                baos.write(buff,0,size);
            }
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        System.out.println(baos.toString());
//        baos.close();

        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(("this is response" + sequence + System.getProperty("line.separator")).getBytes());
        responseBody.close();
    }
}