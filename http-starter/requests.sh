#!/bin/bash
BASE="http://localhost:8080"
CURL_OPTS="-s -w \nHTTP Status: %{http_code}\n"

echo "==========================================="
echo "1. ФИЛЬМЫ"
echo "==========================================="

echo "--- 1.1 Создание фильма (Успех, 201) ---"
curl $CURL_OPTS -X POST $BASE/movies -H "Content-Type: application/json" -d '{"title":"Inception","durationMinutes":148}'
echo

echo "--- 1.2 Создание фильма (Ошибка валидации, 400) ---"
curl $CURL_OPTS -X POST $BASE/movies -H "Content-Type: application/json" -d '{"title":"","durationMinutes":120}'
echo

echo "--- 1.3 Получение списка фильмов (Успех, 200) ---"
curl $CURL_OPTS "$BASE/movies"
echo

echo "--- 1.4 Фильтрация по названию (Успех, 200) ---"
curl $CURL_OPTS "$BASE/movies?title=cep"
echo

echo "--- 1.5 Изменение фильма (Успех, 200) ---"
curl $CURL_OPTS -X PUT $BASE/movies/1 -H "Content-Type: application/json" -d '{"title":"Inception (2010)","durationMinutes":148}'
echo

echo "==========================================="
echo "2. СЕАНСЫ"
echo "==========================================="

START=$(date -u -d "+2 hours" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+2H +"%Y-%m-%dT%H:%M:%SZ")
START_IMAX=$(date -u -d "+1 hour" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+1H +"%Y-%m-%dT%H:%M:%SZ")

echo "--- 2.1 Создание STANDARD сеанса (Успех, 201) ---"
curl $CURL_OPTS -X POST $BASE/sessions -H "Content-Type: application/json" -d "{\"movieId\":1,\"startTime\":\"$START\",\"hallType\":\"STANDARD\",\"price\":500.0}"
echo

echo "--- 2.2 Создание IMAX сеанса (Успех, 201) ---"
curl $CURL_OPTS -X POST $BASE/sessions -H "Content-Type: application/json" -d "{\"movieId\":1,\"startTime\":\"$START_IMAX\",\"hallType\":\"IMAX\",\"price\":900.0}"
echo

echo "--- 2.3 Создание сеанса для несуществующего фильма (404) ---"
curl $CURL_OPTS -X POST $BASE/sessions -H "Content-Type: application/json" -d "{\"movieId\":999,\"startTime\":\"$START\",\"hallType\":\"STANDARD\",\"price\":100}"
echo

echo "--- 2.4 Фильтрация сеансов по типу IMAX (Успех, 200) ---"
curl $CURL_OPTS "$BASE/sessions?hallType=IMAX"
echo

echo "--- 2.5 Фильтрация: только доступные (Успех, 200) ---"
curl $CURL_OPTS "$BASE/sessions?onlyAvailable=true"
echo

echo "==========================================="
echo "3. БИЗНЕС-ЛОГИКА (Покупка билетов)"
echo "==========================================="

echo "--- 3.1 Покупка 3 билетов на STANDARD (Успех, 200) ---"
curl $CURL_OPTS -X POST $BASE/sessions/1/buy -H "Content-Type: application/json" -d '{"count":3}'
echo

echo "--- 3.2 Покупка 100 билетов (Ошибка: нет мест, 409) ---"
curl $CURL_OPTS -X POST $BASE/sessions/1/buy -H "Content-Type: application/json" -d '{"count":100}'
echo

echo "--- 3.3 Покупка на IMAX за 1 час до начала (Успех, 200) ---"
curl $CURL_OPTS -X POST $BASE/sessions/2/buy -H "Content-Type: application/json" -d '{"count":1}'
echo

echo "--- 3.4 Покупка на IMAX за 5 мин до начала (Ошибка: правило 15 мин, 409) ---"
START_SOON=$(date -u -d "+5 minutes" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+5M +"%Y-%m-%dT%H:%M:%SZ")
curl $CURL_OPTS -X POST $BASE/sessions -H "Content-Type: application/json" -d "{\"movieId\":1,\"startTime\":\"$START_SOON\",\"hallType\":\"IMAX\",\"price\":500.0}" > /dev/null
curl $CURL_OPTS -X POST $BASE/sessions/3/buy -H "Content-Type: application/json" -d '{"count":1}'
echo

echo "==========================================="
echo "4. УДАЛЕНИЕ И ОШИБКИ"
echo "==========================================="

echo "--- 4.1 Удаление фильма с сеансами (Ошибка: конфликт, 409) ---"
curl $CURL_OPTS -X DELETE $BASE/movies/1
echo

echo "--- 4.2 Удаление сеанса (Успех, 204) ---"
curl $CURL_OPTS -X DELETE $BASE/sessions/3
echo

echo "--- 4.3 Удаление несуществующего сеанса (404) ---"
curl $CURL_OPTS -X DELETE $BASE/sessions/999
echo

echo "--- 4.4 Неверный метод (405) ---"
curl $CURL_OPTS -X PATCH $BASE/movies
echo