package httpserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by AlxEx on 25.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 * @see com.beust.jcommander.JCommander
 * @see org.apache.log4j.Logger
 * @see io.netty.bootstrap.ServerBootstrap
 */
public class Server {
    /**
     * Номер порта, на котором будет работать сервер.
     */
    private final int port;
    /**
     * Обработчик сессий. Посылает ответ на полученный запрос.
     */
    private static SessionHandler handler;
    /**
     * Основной канал.
     */
    private static Channel socketChannel;
    /**
     * logger
     */
    public static Logger logger = Logger.getLogger(Server.class);
    /**
     * statistic
     */
    public static final Statistic statistic = Statistic.getStatistic();

    /**
     * @param port       - connection port
     * @param numThreads - кол-во параллельных обработчиков
     */
    public Server(int port, int numThreads) {
        this.port = port;
        handler = new SessionHandler(numThreads);
    }

    /**
     * Метод запуска сервера.
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ServerInitializer());
        socketChannel = serverBootstrap.bind(port).channel();
        logger.info("java.httpserver.netty.Server started");
    }

    /**
     * Метод остановки сервера
     */
    public static void stop() {
        if (socketChannel.isOpen()) {
            ChannelFuture cf = socketChannel.close();
            cf.awaitUninterruptibly();
        }
        handler.close();
        logger.info("java.httpserver.netty.Server stopped");
        System.exit(0);
    }

    /**
     * Метод возвращает список последних 16 закрытых сессий.
     *
     * @return список последних 16 закрытых сессий
     */
    public static ConcurrentLinkedDeque<Session> getLastConnection() {
        return statistic.getLastConnection();
    }

    /**
     * Метод возвращает обработчик сессий.
     *
     * @return обработчик сессий
     */
    public static SessionHandler getSessionHandler() {
        return handler;
    }


    public static void addNewSession(Session session) {
        statistic.addConnection(session);
    }
}
