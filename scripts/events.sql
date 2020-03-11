SET GLOBAL event_scheduler = on;

CREATE EVENT IF NOT EXISTS average_power
    ON SCHEDULE EVERY 1 DAY
    COMMENT 'updates device average average power consumption.'
    DO
    UPDATE consumption_ts AS cts
        INNER JOIN (SELECT timeseries_id, IF(ISNULL(AVG(power)), 0, AVG(power)) AS avg_power FROM device_data WHERE power > 10 GROUP BY timeseries_id) AS tmp
        ON cts.timeseries_id = tmp.timeseries_id
    SET cts.default_value = temp.avg_power;

CREATE EVENT IF NOT EXISTS average_power_seven_days
    ON SCHEDULE EVERY 1 HOUR
    COMMENT 'updates device last 7 days average power consumption.'
    DO
    UPDATE consumption_ts AS cts
        INNER JOIN
        (SELECT timeseries_id, IF(ISNULL(AVG(power)), 0, AVG(power)) AS avg_power
         FROM device_data WHERE power > 10 && date >= (DATE(NOW()) - INTERVAL 7 DAY) GROUP BY timeseries_id) AS tmp
        ON cts.timeseries_id = tmp.timeseries_id
    SET cts.average_power_for_prediction = tmp.avg_power;

