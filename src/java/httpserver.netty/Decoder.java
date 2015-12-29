package httpserver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequestDecoder;

import java.util.List;

/**
 * Created by AlxEx on 25.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 */
public class Decoder extends HttpRequestDecoder {

    /**
     * Метод декодирования входного буфера в объекты. Записывает значение размера входного буфера в список
     * декодированных объектов.
     *
     * @param ctx  - контекст приходящий с канала
     * @param list - возращаемый лист обьектов на дальнейшую обработку
     * @param buf  - байтовый буфер считования
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        Integer size = buf.readableBytes();
        super.decode(ctx, buf, list);
        size -= buf.readableBytes();
        list.add(0, size);
    }
}
