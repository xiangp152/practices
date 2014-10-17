package test.armzs.webservice.axis;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Created by xiangpeng on 2014/9/15.
 */
public class AxisTest {

    public static void main(String[] args) {
        String endpoint = "http://localhost:8080/axis/services/sayHello";
        Service service = new Service();
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new URL(endpoint));
            call.addParameter("sayHello", Constants.XSD_STRING, ParameterMode.IN);
            call.setOperationName(new QName("armzs.org", "sayHello"));
            call.setReturnType(new QName("armzs.org", "sayHello"),String.class);
            call.setUsername("user1");
            call.setPassword("pass1");
            String ret = (String) call.invoke(new Object[]{"Hello!"});
            System.out.println("Sent 'Hello!', got '" + ret + "'");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }
}
