# Netty-httpServer
##Task
Необходимо реализовать http-сервер на фреймворке netty
(http://netty.io/), со следующим функционалом:



1. По запросу на http://somedomain/hello отдает «Hello World» через 10 секунд

2. По запросу на http://somedomain/redirect?url=<url> происходит
переадресация на указанный url

3. По запросу на http://somedomain/status выдается статистика:

 - общее количество запросов

 - количество уникальных запросов (по одному на IP)

 - счетчик запросов на каждый IP в виде таблицы с колонкам и IP,
кол-во запросов, время последнего запроса

 - количество переадресаций по url'ам в виде таблицы, с колонками:
url, кол-во переадресаций

 - количество соединений, открытых в данный момент

 - в виде таблицы лог из 16 последних обработанных соединений, колонки
src_ip, URI, timestamp, sent_bytes, received_bytes, speed (bytes/sec)

## Screenshots

1. `/status` page:
![Screenshot1](https://github.com/AlxShcherbak/Netty-httpServer/blob/master/status%20-%20some%20tests%203%20(in%20browser).png)

2. ` ab -c 100 -n 10000 http://localhost:8080/status ` result:
![Screenshot2](https://github.com/AlxShcherbak/Netty-httpServer/blob/master/ab%20test.png)

3. ```/status``` page after ab testing:
![Screenshot3](https://github.com/AlxShcherbak/Netty-httpServer/blob/master/status%20after%20ab%20test.png)

## Some implementation details

Запуск программы производиться через `class ServerStarter`, который инициализирует Logger, считует аргументы запуска приложения и через JCommanderOptions парсерит аргументы, определяет значения порта и количества потоков паралельной обработки, по умолчанию port: 8080, threads: 100, после определения настроек сервера производится создание обьекта класса Server и непосредственно запускаеться сервер server.run().

`class Server` - являеться сущностью сервера произвот запуск и остановку, содержит в себе основной канал и обработчик сессий (requests), так же хранит статистику.

`class ServerHandler extends SimpleChannelInboundHandler<Object>` - являеться классом обработчиком входных сообщений, requests

`ServerInitializer extends ChannelInitializer<SocketChannel>` - класс инициализации обработчиков сервера Кодера, декодера и ServerHandler.

`class Session` - являеться сущность единоразового запроса, сессии. Хранит информацию об запросе, ответе, канале обработки.

`class SessionHandler implements Runnable` - класс служит для обработки и работы с сессиями. Обрабатывает команды приходящие в сессии.

`class Statistic` - класс хранящий информацию об работе сервера : количество запросов, данные про уникальные запросы, последние 16 сессий, информацию об ?redirects. Реализован через паттерн singleton. Связан непосредственно с сервером.

`class JCommanderOptions` - класс карта для парсеринга (разбора) аргументов запуска программы


