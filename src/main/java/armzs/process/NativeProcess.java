package test.armzs.process;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by xiangpeng on 2014/10/15.
 */
public class NativeProcess {

    public static void main(String[] args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("cmd","/K","start");
        Map<String, String> env = pb.environment();
//        env.put("VAR1", "myValue");
//        env.remove("OTHERVAR");
//        env.put("VAR2", env.get("VAR1") + "suffix");
        pb.directory(new File("E:/test"));
        File log = new File("E:/test/log");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
        Process p = pb.start();
        assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
        assert pb.redirectOutput().file() == log;
        assert p.getInputStream().read() == -1;
    }
}
