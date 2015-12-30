package httpserver.netty;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

import java.util.List;

/**
 * Created by AlxEx on 25.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 */
public class JCommanderOptions {
    /**
     * лист неопознаных аргументов
     */
    @Parameter
    public List<String> parameters = Lists.newArrayList();

    /**
     * аргумент запуска программмы, порт
     */
    @Parameter(names = "-port", description = "connection port number")
    public int port = 8080; // default value 8080

    /**
     * аргумент запуска программы, количество потоков паралельной оброботки
     */
    @Parameter(names = "-threads", description = "calculated threads number")
    public int threads = 100; // default value 100

}
