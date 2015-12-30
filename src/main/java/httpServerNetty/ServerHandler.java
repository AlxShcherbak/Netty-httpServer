package httpServerNetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by AlxEx on 25.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 * @see io.netty.channel.ChannelHandler
 */
public class ServerHandler extends SimpleChannelInboundHandler<Object> {
    public static Logger logger = Logger.getLogger(ServerHandler.class);
    /**
     * Пришедший запрос
     */
    private HttpRequest request;
    /**
     * Размер пришедшего запроса
     */
    private Integer size;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * Метод обрабатывает принимаемые объекты, формирует из них сессию и
     * добавляет ее в очередь на обработку.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        // Если объект Integer, записать это значение в размер запроса.
        if (msg instanceof Integer) {
            size = (Integer) msg;
        }
        // Если объект HttpRequest, записать его в значение запроса.
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            if (is100ContinueExpected(request)) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, CONTINUE);
                ctx.writeAndFlush(response);
            }
        }
        // Если объект LastHttpContent, сформировать сессию и отправить ее в
        // очередь на обработку..
        if (msg instanceof LastHttpContent) {
            LastHttpContent trailer = (LastHttpContent) msg;

            if (trailer.getDecoderResult().isSuccess()) {
                Server.getSessionHandler().addSessionToProcess(
                        new Session(ctx, request, size));
            } else {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, BAD_REQUEST);
                ctx.writeAndFlush(response);
            }
        }
    }
}
