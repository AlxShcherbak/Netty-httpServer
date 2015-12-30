package httpserver.netty;

import com.beust.jcommander.JCommander;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by AlxEx on 29.12.2015.
 *
 * @author AlxEx - Alex Shcherba
 */
public class ServerStarter {
    private static Logger logger = Logger.getLogger(ServerStarter.class);

    public static void main(String[] args) {
        //logger settings
        logger.setLevel(Level.INFO);
        BasicConfigurator.configure();

        // ������� ���������� ��������� ������.
        JCommanderOptions options = new JCommanderOptions();
        new JCommander(options, args);

        //init port and numb threads
        int port = options.port;
        int numThreads = options.threads;


        logger.info("connection port : " + port);
        logger.info("threads number : " + numThreads);


        // ������ �������.
        Server server = new Server(port, numThreads);
        try {
            server.run();
        } catch (InterruptedException e) {
            logger.info("Error. java.httpserver.netty.Server not started.");
            e.printStackTrace();
        }
    }
}
