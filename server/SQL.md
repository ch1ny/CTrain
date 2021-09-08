# SQL说明

## Table

### User：用户信息表

* user_id：用户身份证号
* user_name：用户姓名
* user_password：用户密码

### Passengers：乘客表

* user_id：用户身份证号
* passengers_id：关联身份证号
* passengers_name：关联人姓名

### Station：车站表

* station_id：车站代号
* station_name：车站名称
* location：车站所在城市

### Train：列车信息表

* train_id：列车车次
* train_type：列车类型
* train_start：始发站
* train_end：终点站

### TrainStation：列车到达车站表

* train_id：列车车次
* station_id：车站代号
* arrive_time：到站时刻
* leave_time：离站时刻

### TrainSeats：列车座位表

* train_id：列车车次
* carriage_id：车厢号
* seat_type：座位类型
* seat_num：座位数量

### TrainTickets：购票表

* tickets_id：订单编号
* user_id：购票人身份证号
* passenger_id：乘客身份证号
* train_id：列车车次
* off_date：发车日期 **（这里存的是列车的发车日期，而不是乘客真实的上车日期）**
* start_station：出发站点
* end_station：目的站点
* carriage_id：车厢号
* seat_id：座次号

# 核心SQL

## 查询从A到B的火车

```sql
SELECT X.train_id
FROM (
	SELECT A.train_id, A.stop_index
    FROM TrainStation A
    WHERE EXISTS (
        SELECT 1
        FROM Stations B
        WHERE A.station_name = B.station_name AND B.location = "南昌"
    )
) X
INNER JOIN (
	SELECT A.train_id, A.stop_index
    FROM TrainStation A
    WHERE EXISTS (
        SELECT 1
        FROM Stations B
        WHERE A.station_name = B.station_name AND B.location = "济南"
    )
) Y
ON X.train_id = Y.train_id
WHERE X.stop_index < Y.stop_index
```

## 查询从A到B的转乘路线

* 起始站能到达的站点

  ```sql
  SELECT DISTINCT station_name
  FROM TrainStation A
  INNER JOIN (
  	SELECT train_id, stop_index
      FROM TrainStation A
      WHERE EXISTS (
          SELECT 1
          FROM Stations B
          WHERE A.station_name = B.station_name AND B.location = "南昌"
      )
  ) B
  ON A.train_id = B.train_id
  WHERE A.stop_index > B.stop_index
  ```

* 起始站能到达的城市

  ```sql
  SELECT DISTINCT location
  FROM Stations A
  WHERE EXISTS (
      SELECT 1
      FROM (
          SELECT DISTINCT station_name
          FROM TrainStation A
          INNER JOIN (
              SELECT train_id, stop_index
              FROM TrainStation A
              WHERE EXISTS (
                  SELECT 1
                  FROM Stations B
                  WHERE A.station_name = B.station_name AND B.location = "南昌"
              )
          ) B
          ON A.train_id = B.train_id
          WHERE A.stop_index > B.stop_index
      ) B
      WHERE A.station_name = B.station_name
  )
  ```

* 能到达终点站的站点

  ```sql
  SELECT DISTINCT station_name
  FROM TrainStation A
  INNER JOIN (
  	SELECT train_id, stop_index
      FROM TrainStation A
      WHERE EXISTS (
          SELECT 1
          FROM Stations B
          WHERE A.station_name = B.station_name AND B.location = "吉林"
      )
  ) B
  ON A.train_id = B.train_id
  WHERE A.stop_index < B.stop_index
  ```

* 能到达终点站的城市

  ```sql
  SELECT DISTINCT location
  FROM Stations A
  WHERE EXISTS (
      SELECT 1
      FROM (
          SELECT DISTINCT station_name
          FROM TrainStation A
          INNER JOIN (
              SELECT train_id, stop_index
              FROM TrainStation A
              WHERE EXISTS (
                  SELECT 1
                  FROM Stations B
                  WHERE A.station_name = B.station_name AND B.location = "吉林"
              )
          ) B
          ON A.train_id = B.train_id
          WHERE A.stop_index < B.stop_index
      ) B
      WHERE A.station_name = B.station_name
  )
  ```

* 寻找中转城市

  ```sql
  SELECT location
  FROM (
      SELECT DISTINCT location
      FROM Stations A
      WHERE EXISTS (
          SELECT 1
          FROM (
              SELECT station_name
              FROM TrainStation A
              INNER JOIN (
                  SELECT train_id, stop_index
                  FROM TrainStation A
                  WHERE EXISTS (
                      SELECT 1
                      FROM Stations B
                      WHERE A.station_name = B.station_name AND B.location = "南昌"
                  )
              ) B
              ON A.train_id = B.train_id
              WHERE A.stop_index > B.stop_index
          ) B
          WHERE A.station_name = B.station_name
      )
  ) A
  WHERE EXISTS (
  	SELECT 1
      FROM (
          SELECT DISTINCT location
          FROM Stations A
          WHERE EXISTS (
              SELECT 1
              FROM (
                  SELECT DISTINCT station_name
                  FROM TrainStation A
                  INNER JOIN (
                      SELECT train_id, stop_index
                      FROM TrainStation A
                      WHERE EXISTS (
                          SELECT 1
                          FROM Stations B
                          WHERE A.station_name = B.station_name AND B.location = "吉林"
                      )
                  ) B
                  ON A.train_id = B.train_id
                  WHERE A.stop_index < B.stop_index
              ) B
              WHERE A.station_name = B.station_name
          )
      ) B
      WHERE A.location = B.location
  )
  ```

* 完整代码1（需要寻找中转城市以及两列火车车号） ***同城换乘***

  ```sql
  SELECT A.location, A.train_id AS from_id, A.station_name AS off_station, B.train_id AS to_id, B.station_name AS on_station, from_station, to_station
  FROM (
      SELECT location, A.station_name, B.train_id, from_station
      FROM Stations A
      INNER JOIN (
     		SELECT station_name, A.train_id, from_station
          FROM TrainStation A
          INNER JOIN (
          	SELECT train_id, stop_index, A.station_name AS from_station
              FROM TrainStation A
              WHERE EXISTS (
              	SELECT 1
  	            FROM Stations B
                  WHERE A.station_name = B.station_name AND B.location = "南昌"
  			)
  		) B
          ON A.train_id = B.train_id
          WHERE A.stop_index > B.stop_index
      ) B
      ON A.station_name = B.station_name
  ) A
  INNER JOIN (
      SELECT location, A.station_name, B.train_id, to_station
      FROM Stations A
      INNER JOIN (
     		SELECT station_name, A.train_id, to_station
          FROM TrainStation A
          INNER JOIN (
          	SELECT train_id, stop_index, A.station_name AS to_station
              FROM TrainStation A
              WHERE EXISTS (
              	SELECT 1
  	            FROM Stations B
                  WHERE A.station_name = B.station_name AND B.location = "吉林"
  			)
  		) B
          ON A.train_id = B.train_id
          WHERE A.stop_index < B.stop_index
      ) B
      ON A.station_name = B.station_name
  ) B
  ON A.location = B.location
  ```

* 完整代码2（需要寻找中转车站以及两列火车车号）***同站换乘***

  ```sql
  SELECT  A.station_name AS transfer_station, A.train_id AS from_id, B.train_id AS to_id, from_station, to_station
  FROM (
      SELECT station_name, A.train_id, from_station
  	FROM TrainStation A
      INNER JOIN (
      	SELECT train_id, stop_index, A.station_name AS from_station
          FROM TrainStation A
          WHERE EXISTS (
          	SELECT 1
  	        FROM Stations B
              WHERE A.station_name = B.station_name AND B.location = "南昌"
  		)
  	) B
      ON A.train_id = B.train_id
      WHERE A.stop_index > B.stop_index
  ) A
  INNER JOIN (
      SELECT station_name, A.train_id, to_station
  	FROM TrainStation A
      INNER JOIN (
      	SELECT train_id, stop_index, A.station_name AS to_station
          FROM TrainStation A
          WHERE EXISTS (
          	SELECT 1
  	        FROM Stations B
              WHERE A.station_name = B.station_name AND B.location = "吉林"
  		)
  	) B
      ON A.train_id = B.train_id
      WHERE A.stop_index < B.stop_index
  ) B
  ON A.station_name = B.station_name
  ```

  

## 查询余票

* 所需数据：
  * train_id：火车车次
  * date：出行日期
  * from_station：出发车站
  * to_station：目标车站

* 查询某列车的各座位数

```sql
SELECT seat_type, COUNT(*) num
FROM Seats
WHERE train_id = ?
GROUP BY seat_type
```

* 某区间内允许购买的车票所具备的特征：

  * 其它车票下车地点<=上车地点 || 其它车票上车地点>=下车地点

    * 该趟列车在特定出发日期的车票
    
    ```sql
    SELECT from_station, to_station, carriage_id, seat_id
    FROM Tickets
    WHERE train_id = ? AND off_date = ?
    ```
    
    * 出发车站的车站序号以及出发天数
    
    ```sql
    SELECT stop_index from_index, day_num - 1 days
    FROM TrainStation
    WHERE train_id = ? AND station_name = ?
    ```
    
    * 到达车站的车站序号
    
    ```sql
    SELECT stop_index to_index
    FROM TrainStation
    WHERE train_id = ? AND station_name = ?
    ```
    
    * 查找该列车所有车票的上下站序号及座位编号
    
    ```sql
    SELECT A.stop_index from_index, B.stop_index to_index, carriage_id, seat_id
    FROM (
    	SELECT station_name, stop_index
        FROM TrainStation
        WHERE train_id = ?
    ) A
    INNER JOIN (
    	SELECT station_name, stop_index
        FROM TrainStation
        WHERE train_id = ?
    ) B
    INNER JOIN (
    	SELECT from_station, to_station, carriage_id, seat_id
        FROM Tickets
        WHERE train_id = ? AND off_date = ?
    ) C
    ON A.station_name = C.from_station AND B.station_name = C.to_station
    ```
    
    * 查找该列车所有车票的上下站序号及座位编号以及座位类型
    
    ```sql
    SELECT A.stop_index from_index, B.stop_index to_index, carriage_id, seat_id, seat_type
    FROM (
    	SELECT station_name, stop_index
        FROM TrainStation
        WHERE train_id = ?
    ) A
    INNER JOIN (
    	SELECT station_name, stop_index
        FROM TrainStation
        WHERE train_id = ?
    ) B
    INNER JOIN (
    	SELECT A.from_station, A.to_station, A.carriage_id, A.seat_id, B.seat_type
        FROM Tickets A
        INNER JOIN (
        	SELECT DISTINCT train_id, carriage_id, seat_type
            FROM Seats
            WHERE train_id = ?
        ) B
        ON A.train_id = B.train_id
        WHERE off_date = ?
    ) C
    ON A.station_name = C.from_station AND B.station_name = C.to_station
    ```
    
    * 获取余票（完整代码）
    
    ```sql
    SELECT seat_type, COUNT(*) num
    FROM (
    	SELECT seat_type, carriage_id, seat_id, from_index, to_index
        FROM Seats A
        INNER JOIN (
            SELECT A.stop_index from_index, B.stop_index to_index, A.train_id
            FROM TrainStation A
            INNER JOIN TrainStation B
            ON A.train_id = B.train_id
            WHERE A.train_id = ? AND A.station_name = ? AND B.station_name = ?
        ) B
        ON A.train_id = B.train_id
    ) A
    LEFT OUTER JOIN (
        SELECT A.stop_index from_index, B.stop_index to_index, carriage_id, seat_id
        FROM (
            SELECT station_name, stop_index
            FROM TrainStation
            WHERE train_id = ?
        ) A
        INNER JOIN (
            SELECT station_name, stop_index
            FROM TrainStation
            WHERE train_id = ?
        ) B
        INNER JOIN (
            SELECT from_station, to_station, carriage_id, seat_id
            FROM Tickets
            WHERE train_id = ? AND off_date = ?
        ) C
        ON A.station_name = C.from_station AND B.station_name = C.to_station
    ) B
    ON A.carriage_id = B.carriage_id AND A.seat_id = B.seat_id
    WHERE B.seat_id IS NULL OR A.from_index >= B.to_index OR A.to_index <= B.from_index
    GROUP BY A.seat_type
    ```
    

##  购票

* 查询空座

```sql
SELECT A.carriage_id, A.seat_id
FROM (
	SELECT carriage_id, seat_id, from_index, to_index
    FROM Seats A
    INNER JOIN (
        SELECT A.stop_index from_index, B.stop_index to_index, A.train_id
        FROM TrainStation A
        INNER JOIN TrainStation B
        ON A.train_id = B.train_id
        WHERE A.train_id = ? AND A.station_name = ? AND B.station_name = ?
    ) B
    ON A.train_id = B.train_id
    WHERE seat_type = ?
) A
LEFT OUTER JOIN (
    SELECT A.stop_index from_index, B.stop_index to_index, carriage_id, seat_id
    FROM (
        SELECT station_name, stop_index
        FROM TrainStation
        WHERE train_id = ?
    ) A
    INNER JOIN (
        SELECT station_name, stop_index
        FROM TrainStation
        WHERE train_id = ?
    ) B
    INNER JOIN (
        SELECT from_station, to_station, carriage_id, seat_id
        FROM Tickets
        WHERE train_id = ? AND off_date = ?
    ) C
    ON A.station_name = C.from_station AND B.station_name = C.to_station
) B
ON A.carriage_id = B.carriage_id AND A.seat_id = B.seat_id
WHERE B.seat_id IS NULL OR A.from_index >= B.to_index OR A.to_index <= B.from_index
```



