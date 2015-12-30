package httpServerNetty;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by AlxEx on 26.12.2015.
 *
 * @author AlxEx - Alex Shcherbak
 */
public class Statistic {
    private static final Statistic statistic = new Statistic();

    public static Statistic getStatistic() {
        return statistic;
    }

    private Statistic() {
    }


    /**
     * Кол-во открытых соединений.
     */
    private static AtomicInteger currentConnection = new AtomicInteger(0);
    /**
     * Список 16 последних соединений
     */
    private ConcurrentLinkedDeque<Session> lastConnection = new ConcurrentLinkedDeque<Session>() {
        /**
         * добавление сессии в список последних 16
         */
        @Override
        public void addFirst(Session session) {
            // поддержание в списке не более 16 элементов
            while (size() >= 16)
                removeLast();
            super.addFirst(session);
        }
    };
    /**
     * Общее число подключенийк серверу
     */
    private AtomicInteger totalConnectionsNumber = new AtomicInteger(0);
    /**
     * карта уникальных подключений (по IP)
     */
    private ConcurrentMap<InetAddress, RequestCounter> uniqueRequests = new ConcurrentHashMap<InetAddress, RequestCounter>() {
        /**
         * @param key - ip address of session
         * @param value - request counter
         * @return the previous value associated with {@code key}, or
         *         {@code null} if there was no mapping for {@code key}
         */
        @Override
        public RequestCounter put(InetAddress key, RequestCounter value) {
            if (containsKey(key)) {
                synchronized (this) {
                    return super.put(key, get(key).addRequest(value));
                }
            } else return super.put(key, value);
        }
    };
    /**
     * статистика по количеству вызываемых запросов (url)
     */
    private ConcurrentMap<String, Integer> urlRedirects = new ConcurrentHashMap<String, Integer>() {
        /**
         * @param key - url address of redirect
         * @param value - number of redirects to this url
         * @return the previous value associated with {@code key}, or
         *         {@code null} if there was no mapping for {@code key}
         */
        @Override
        public Integer put(String key, Integer value) {
            if (containsKey(key)) {
                synchronized (this) {
                    return super.put(key, get(key) + 1);
                }
            } else {
                return super.put(key, 1);
            }
        }
    };

    /**
     * Обработка сессии - добавление ее в статистику
     *
     * @param session - сессия для обработки
     */
    public synchronized void addConnection(Session session) {
        // добавление сессии в список последних 16 активных сессий
        statistic.lastConnection.addFirst(session);
        // инкриминтация общего количества соединений
        statistic.totalConnectionsNumber.incrementAndGet();
        // добавление информации про сессию в список уникальных сессий
        statistic.uniqueRequests.put(session.remoteAddress.getAddress(), new RequestCounter(session));
        // добавление информации про сессию в список url переадресаций
        statistic.urlRedirects.put(session.getCommand(), 1);
    }

    public ConcurrentLinkedDeque<Session> getLastConnection() {
        return statistic.lastConnection;
    }

    public int getTotalConnectionsNumber() {
        return statistic.totalConnectionsNumber.get();
    }

    public ConcurrentMap<InetAddress, RequestCounter> getUniqueRequests() {
        return statistic.uniqueRequests;
    }

    public ConcurrentMap<String, Integer> getUrlRedirects() {
        return statistic.urlRedirects;
    }

    /**
     * Метод добавляет открытое соединение.
     */
    public static void addOpenConnection() {
        currentConnection.incrementAndGet();
    }

    /**
     * Метод удаляет открытое соединение.
     */
    public static void removeOpenConnection() {
        currentConnection.decrementAndGet();
    }

    /**
     * Метод возвращает кол-во открытых соединений.
     *
     * @return Кол-во открытых соединений
     */
    public static int getCurrentConnection() {
        return currentConnection.get();
    }

    /**
     * класс учета уникальных подключений (запросов)
     */
    class RequestCounter {
        /**
         * количество подключений (запроса) с данного IP
         */
        private AtomicInteger requestsNumbers = new AtomicInteger(0);
        /**
         * время последнего подключения (запроса) с даного IP
         */
        private AtomicLong timeLastRequest;
        /**
         * адресс с которого прозходило подключение
         */
        private final InetAddress ipAddress;

        public RequestCounter(Session session) {
            this.ipAddress = session.remoteAddress.getAddress();
            requestsNumbers = new AtomicInteger(1);
            timeLastRequest = new AtomicLong(session.getTime());
        }

        public RequestCounter addRequest(RequestCounter counter) {
            synchronized (this) {
                requestsNumbers.incrementAndGet();
                this.timeLastRequest = counter.timeLastRequest;
            }
            return this;
        }

        public InetAddress getIpAddress() {
            return ipAddress;
        }

        public long getTimeLastRequest() {
            return timeLastRequest.get();
        }

        public int getRequestsNumbers() {
            return requestsNumbers.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RequestCounter that = (RequestCounter) o;

            return !(ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null);

        }

        /**
         * хеш код определяеться исходя с IP адреса подключения
         */
        @Override
        public int hashCode() {
            return ipAddress != null ? 31 * Arrays.hashCode(ipAddress.getAddress()) : 0;
        }
    }
}
