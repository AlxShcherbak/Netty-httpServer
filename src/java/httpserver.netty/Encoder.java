package httpserver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.List;

/**
 * Created by AlxEx on 25.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 */
public class Encoder extends HttpResponseEncoder {
    /**
     * Сессия, сообщение из которой отсылается и в которую передается размер
     * этого сообщения.
     */
    private Session session = null;

    /**
     * Метод кодирования отсылаемого сообщения. Извлекает размер сообщения.
     *
     * @param ctx  - контекст отправляемого канала
     * @param list - лист обектов для записи в байтбуффер
     * @param msg  - сообщение для записи
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> list) throws Exception {
        super.encode(ctx, msg, list);
        for (Object obj : list) {
            ByteBuf b = (ByteBuf) obj;
            session.sizeOut += b.readableBytes();
        }
    }

    /**
     * Метод записи объекта. Устанавливает сессию и вызывает метод кодирования
     * для отсылаемого сообщения из сессии.
     *
     * @param ctx - контекст отправляемого канала
     * @param cp  -
     * @param msg - сообщение для отправк - сессися
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise cp) throws Exception {
        if (msg instanceof Session) {
            session = (Session) msg;
        }
        super.write(ctx, session.response, cp);
    }
}
