package httpserver.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.net.InetSocketAddress;
import java.util.Date;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by AlxEx on 25.12.2015.
 */
public class Session {
    /**
     * Контекст канала, в который будет отсылаться ответ.
     */
    private ChannelHandlerContext ctx;
    /**
     * Полученный запрос.
     */
    public HttpRequest request;
    /**
     * Сформированный ответ. По умолчанию BAD_REQUEST.
     */
    public HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
            BAD_REQUEST);
    /**
     * IP адрес отправителя запроса.
     */
    public InetSocketAddress remoteAddress;
    /**
     * Время получения запроса.
     */
    private long requestTime;
    /**
     * Время отправки ответа.
     */
    private long responseTime;
    /**
     * Размер запроса в байтах.
     */
    public int sizeIn;
    /**
     * Размер ответа в байтах.
     */
    public int sizeOut;

    /**
     * Конструктор. Создает сессию, добавляет открытое соединение.
     */
    public Session(ChannelHandlerContext ctx, HttpRequest request, int sizeIn) {
        this.ctx = ctx;
        this.request = request;
        this.remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        this.sizeIn = sizeIn;
        this.requestTime = System.currentTimeMillis();
        Server.statistic.addOpenConnection();
    }

    /**
     * Метод возвращает значение URI из запроса. URI необходимо для принятия
     * решение о том какой ответ необходим.
     */
    public String getCommand() {
        return request.getUri();
    }

    /**
     * Метод возвращает время получения запроса.
     */
    public long getTime() {
        return requestTime;
    }

    /**
     * Метод меняет значение ответа на переданное и отправляет сессию на запись.
     * Также закрывает соединение и обновляет список последних 16 закрытых
     * соединений.
     *
     * @param response - http response
     */
    public void sendResponse(FullHttpResponse response) {
        this.response = response;
        ctx.writeAndFlush(this);
        responseTime = System.currentTimeMillis();
        ctx.close();
        Server.statistic.removeOpenConnection();
        Server.addNewSession(this);
    }

    /**
     * Метод возвращает IP адрес отправителя запроса.
     */
    public String getRemoteAddress() {
        return remoteAddress.getHostName();
    }

    /**
     * Метод возвращает статус сессии: IP адрес отправителя запроса, URI
     * запроса, timestamp запроса и ответа, размер запроса и ответа.
     */
    public String getStatusString() {
        double speed = (responseTime - requestTime) >= 0 ? (double)(sizeIn + sizeOut) / (responseTime - requestTime) : 0;
        return String.format("%-20s", getRemoteAddress()) + " | " +
                String.format("%-35s", getCommand()) + " | " +
                String.format("%-30s", new Date(requestTime)) + " | " +
                String.format("%-30s", new Date(responseTime)) + " | " +
                String.format("%-10s", sizeIn) + " | " +
                String.format("%-10s", sizeOut) + " | " +
                String.format("%10.2f", speed) + " | ";
    }
}
