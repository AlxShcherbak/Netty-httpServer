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


