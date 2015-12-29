package httpserver.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by AlxEx on 25.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 */
public class SessionHandler implements Runnable {
    /**
     * Очередь сессий готовых к обработке.
     */
    private final BlockingQueue<Session> sessionQueue;
    /**
     * Обработчик паралелльных потоков.
     */
    private final ExecutorService threadPool;
    /**
     * Кол-во параллельных потоков.
     */
    private final int threadPoolSize;

    public SessionHandler(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.sessionQueue = new LinkedBlockingQueue<Session>();
        initThreadPool();
    }

    /**
     * Инициализирует параллельные потоки.
     */
    private void initThreadPool() {
        for (int i = 0; i < threadPoolSize; i++) {
            threadPool.execute(this);
        }
    }

    /**
     * Добавляет сессию в очередь на обработку.
     */
    public void addSessionToProcess(Session session) {
        sessionQueue.add(session);
    }


    /**
     * Метод run, выполняющийся в параллельных потоках.
     */
    public void run() {
        // Обработка сессий из очереди.
        while (true) {
            // Считывает сессию из очереди.
            Session session = null;
            try {
                session = sessionQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Обработка запроса /hello.
            if (session.getCommand().equalsIgnoreCase("/hello")) {
                sendHello(session);
            } else {
                // Обработка запроса /status.
                if (session.getCommand().equalsIgnoreCase("/status")) {
                    sendStatus(session);
                } else {
                    // Обработка запроса /stop.
                    if (session.getCommand().equalsIgnoreCase("/stop")) {
                        Server.stop();
                    } else {
                        // Обработка запроса /redirect.
                        if (session.getCommand().length() > 13
                                && session.getCommand().substring(0, 14)
                                .equalsIgnoreCase("/redirect?url=")) {
                            sendRedirect(session);
                        } else {
                            // Обработка других запросов.
                            send404NotFound(session);
                        }
                    }
                }
            }
        }
    }

    /**
     * Перенаправление пользователя на другой сайт
     *
     * @param session - абрабатываемая сессия
     */
    private void sendRedirect(Session session) {
        URI uri = null;
        try {
            uri = new URI(session.getCommand().substring(14));
        } catch (URISyntaxException e) {
            session.sendResponse(new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
        }
        if (uri != null) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    MOVED_PERMANENTLY);
            String redirect = uri.toString();
            if (uri.getScheme() == null) {
                redirect = "http://" + redirect;
                response.headers().set(LOCATION, redirect);
            } else {
                response.headers().set(LOCATION, redirect);
            }
            session.sendResponse(response);
        }
    }

    /**
     * Возрат и вывод статуса сервера
     *
     * @param session - обрабатываемая сессия
     */
    private void sendStatus(Session session) {
        StringBuilder status = new StringBuilder();
        // Подсчет общего кол-ва запросов.
        status.append("Общее кол-во запросов: ").append(Server.statistic.getTotalConnectionsNumber())
                .append("\n");

        // Подсчет кол-ва уникальных запросов.
        status.append("\nКол-во уникальных запросов: ")
                .append(Server.statistic.getUniqueRequests().size()).append("\n\n");

        // Формирование таблицы статистики по IP.
        status.append(String.format("%-40s", "IP")).append("|")
                .append(String.format("%-10s", "COUNT")).append("|")
                .append(String.format("%-20s", "LAST TIME")).append("\n");

        Set<InetAddress> ipAddressSetKey = Server.statistic.getUniqueRequests().keySet();
        for (InetAddress inAddr : ipAddressSetKey) {
            status.append(String.format("%-40s", Server.statistic.getUniqueRequests().get(inAddr).getIpAddress()))
                    .append("|")
                    .append(String.format("%-10s", Server.statistic.getUniqueRequests().get(inAddr).getRequestsNumbers()))
                    .append("|")
                    .append(String.format("%-20s", new Date(Server.statistic.getUniqueRequests().get(inAddr).getTimeLastRequest())))
                    .append("\n");
        }
        status.append("\n");

        // Формирование таблицы переадресаций.
        status.append(String.format("%-40s", "URL"))
                .append("|")
                .append(String.format("%-10s", "COUNT"))
                .append("\n");
        Set<String> redKeysSet = Server.statistic.getUrlRedirects().keySet();
        for (String str : redKeysSet) {
            status.append(String.format("%-40s", str))
                    .append("|")
                    .append(String.format("%-10s", Server.statistic.getUrlRedirects().get(str)))
                    .append("\n");
        }
        // Кол-во открытых соединений.
        status.append("\nКол-во текущих соединений: ")
                .append(Server.statistic.getCurrentConnection())
                .append("\n");
        // Статус последних 16 закрытых соединений.
        status.append("\nСтатус последних 16 сессий:\n");
        status.append(String.format("%-20s", "IP"))
                .append(" | ")
                .append(String.format("%-35s", "URL"))
                .append(" | ")
                .append(String.format("%-30s", "req time"))
                .append(" | ")
                .append(String.format("%-30s", "resp time"))
                .append(" | ")
                .append(String.format("%-10s", "Rec bytes"))
                .append(" | ")
                .append(String.format("%-10s", "Send bytes"))
                .append(" | ")
                .append(String.format("%-10s", "Speed kb/s"))
                .append(" | \n");
        for (Session ses : Server.getLastConnection()) {
            status.append(ses.getStatusString())
                    .append("\n");
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.copiedBuffer(status.toString(),
                        Charset.forName("UTF-8")));
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONNECTION, CLOSE);
        session.sendResponse(response);
    }

    private void send404NotFound(Session session) {
        StringBuilder status = new StringBuilder();
        status.append("404 Not Found");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.copiedBuffer(status.toString(),
                        Charset.forName("UTF-8")));
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONNECTION, CLOSE);
        session.sendResponse(response);
    }

    /**
     * Метод обработки запроса /hello. Отправляет ответ в виде строки <<Hello world>>
     *
     * @param session - current session
     * @see FullHttpResponse
     * @see DefaultFullHttpResponse
     */
    private void sendHello(Session session) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.copiedBuffer("<<Hello world>>".toCharArray(), Charset.forName("UTF-8")));
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONNECTION, CLOSE);
        long responseTime = session.getTime() + 10000;
        while (System.currentTimeMillis() < responseTime) ;
        session.sendResponse(response);
    }

    /**
     * закрыкие пула потоков обработки
     */
    public void close() {
        if (!threadPool.isShutdown())
            threadPool.shutdown();
    }
}
