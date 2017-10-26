package doppler.peasy.bootstrap;

import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author doppler
 * @date 2017/10/26
 */
public class Main {
    final static String WEBAPP_PATH = "src/main/webapp/";
    final static int PORT_DEFAULT = 6324;

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = PORT_DEFAULT;
        }
        Server server = new Server(port);
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(WEBAPP_PATH + "/WEB-INF/web.xml");
        root.setResourceBase(WEBAPP_PATH);

        root.setParentLoaderPriority(true);


        root.setClassLoader(Thread.currentThread().getContextClassLoader());
        root.setConfigurationDiscovered(true);


        HandlerCollection hc = new HandlerCollection();
        hc.setHandlers(new Handler[]{new ResponseHandler(), root});
        server.setHandler(hc);

        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);

        server.start();
        server.join();
    }
}
