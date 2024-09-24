package com.kadmuffin.bikesarepain.client.serial;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.ClientConfig;
import dev.architectury.platform.Platform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;

public class DataProcessor {
    public enum SQLState {
        OTHER(0),
        SUCCESS(1),
        TABLE_VERSION_IS_INCOMPATIBLE(2);

        private final int value;

        SQLState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SQLState fromInt(int value) {
            for (SQLState failure : SQLState.values()) {
                if (failure.getValue() == value) {
                    return failure;
                }
            }
            return OTHER;
        }
    }

    public static class DataPoint {
        // Timestamp in milliseconds (since data collection started)
        public long timestamp;
        // Speed in km/h (at this point in time)
        public double speed;
        // Distance moved in this time interval in meters
        public double distance;
        // The calories burned in the time period
        public double calories;

        public DataPoint(long timestamp, double speed, double distance, double calories) {
            this.timestamp = timestamp;
            this.speed = speed;
            this.distance = distance;
            this.calories = calories;
        }

        public boolean isZero() {
            return speed == 0 && distance == 0 && calories == 0;
        }

        public boolean isEquals(DataPoint other) {
            return timestamp == other.timestamp && speed == other.speed && distance == other.distance && calories == other.calories;
        }

        public String toString() {
            return "DataPoint{timestamp=" + timestamp + ", speed=" + speed + ", distance=" + distance + ", calories=" + calories + "}";
        }
    }

    private final Queue<Float> speedAvgQueue;
    private List<DataPoint> dataPoints;
    private final List<Consumer<DataPoint>> onChangeListeners;
    private final List<Consumer<DataPoint>> onNothingChangedListeners;
    private float sumSpeed;
    private double totalCalories;
    private double totalDistance;
    private float wheelRadius;

    // Round to nearest "nice" interval (1, 2, 5, 10, 15, 30 seconds, 1, 2, 5, 10, 15, 30 minutes, 1 hour, 6 hours, 12 hours, 1 day)
    public static final long[] niceIntervals = {
            1000, 2000, 5000, 10000, 15000, 30000, 60000,
            120000, 300000, 600000, 900000, 1800000, 3600000,
            21600000, 43200000, 86400000
    };

    public DataProcessor() {
        speedAvgQueue = new LinkedList<>();
        dataPoints = new ArrayList<>();
        onChangeListeners = new ArrayList<>();
        onNothingChangedListeners = new ArrayList<>();
        this.reset();
    }

    public void addChangeListener(Consumer<DataPoint> listener) {
        onChangeListeners.add(listener);
    }

    public void removeChangeListener(Consumer<DataPoint> listener) {
        onChangeListeners.remove(listener);
    }

    public void addNothingChangedListener(Consumer<DataPoint> listener) {
        onNothingChangedListeners.add(listener);
    }

    public void removeNothingChangedListener(Consumer<DataPoint> listener) {
        onNothingChangedListeners.remove(listener);
    }

    public void reset() {
        sumSpeed = 0;
        totalCalories = 0;
        totalDistance = 0;
        wheelRadius = 0;
        speedAvgQueue.clear();
        dataPoints.clear();
    }

    // It will combine existing points into one point if the distance between them is less than the threshold
    // For the speed, it will take the average of the speeds
    public void compressStreamingPoints(float threshold, int maxPointsPerCompression) {
        if (dataPoints.size() < 2) {
            return;
        }

        LinkedList<DataPoint> compressedPoints = new LinkedList<>();
        DataPoint lastPoint = dataPoints.getFirst();

        for (int i = maxPointsPerCompression; i < dataPoints.size(); i++) {
            DataPoint currentPoint = dataPoints.get(i);

            double distanceDiff = currentPoint.distance - lastPoint.distance;

            if (distanceDiff < threshold) {
                // Modify the last point directly to reduce object creation
                lastPoint.speed = (lastPoint.speed + currentPoint.speed) / 2;
                lastPoint.timestamp = currentPoint.timestamp;  // Update timestamp to the latest one
                lastPoint.distance = currentPoint.distance;
                lastPoint.calories = currentPoint.calories;
            } else {
                compressedPoints.add(lastPoint);  // Add the last compressed point
                lastPoint = currentPoint;  // Move to the next point
            }
        }

        compressedPoints.add(lastPoint);  // Add the final point

        dataPoints = compressedPoints;  // Update the dataPoints with compressed results
    }

    public SQLState loadFromSQLite(Statement database, String id) throws RuntimeException {
        try {
            database.setQueryTimeout(30);

            ResultSet rs = database.executeQuery("SELECT * FROM " + id);

            while (rs.next()) {
                dataPoints.add(new DataPoint(
                        rs.getLong("timestamp"),
                        rs.getDouble("speed"),
                        rs.getDouble("distance"),
                        rs.getDouble("calories")
                ));
            }

            // Load the calories from "id_summary" table
            rs = database.executeQuery("SELECT * FROM " + id + "_summary");
            if (rs.next()) {
                totalCalories = rs.getFloat("calories");
                totalDistance = rs.getFloat("distance");
            }

            return SQLState.SUCCESS;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SQLState saveToSQLite(Statement database, String id) throws RuntimeException {
        // Almost always, we will need to create a new table
        // for this ID, in the case, that we are not,
        // we will check if this table isn't of higher version
        // than this mod's version, and if it is, we will
        // fail to save the data
        String MOD_VERSION = Platform.getMod(BikesArePain.MOD_ID).getVersion();

        try {
            database.setQueryTimeout(30);

            database.executeUpdate("CREATE TABLE IF NOT EXISTS " + id + " (timestamp INTEGER PRIMARY KEY, speed REAL, distance REAL, calories REAL, version TEXT)");

            // Check the version of the last entry (if there is)
            // If the version is higher than the mod's version, we will fail
            ResultSet rs = database.executeQuery("SELECT version FROM " + id + " ORDER BY timestamp DESC LIMIT 1");
            if (rs.next()) {
                String version = rs.getString("version");
                if (version != null && !version.equals(MOD_VERSION)) {
                    return SQLState.TABLE_VERSION_IS_INCOMPATIBLE;
                }
            }

            // Now we add our contents to the database
            for (DataPoint point : dataPoints) {
                database.executeUpdate(
                        String.format("INSERT INTO %s (timestamp, speed, distance, calories, version) VALUES (%d, %f, %f, %f, '%s')",
                                id, point.timestamp, point.speed, point.distance, point.calories, MOD_VERSION));
            }

            // Save the summary
            database.executeUpdate("CREATE TABLE IF NOT EXISTS " + id + "_summary (calories REAL, distance REAL, version TEXT)");

            database.executeUpdate(
                    String.format("INSERT INTO %s_summary (calories, distance, version) VALUES (%f, %f, '%s')",
                            id, totalCalories, totalDistance, MOD_VERSION));

            return SQLState.SUCCESS;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void aggregateData(long intervalMs) {
        if (dataPoints.isEmpty()) return;

        List<DataPoint> aggregatedPoints = new ArrayList<>();
        double speedSum = 0;
        double distanceSum = 0;
        double caloriesSum = 0;
        int count = 0;
        long intervalStart = dataPoints.getFirst().timestamp;

        for (DataPoint point : dataPoints) {
            if (point.timestamp - intervalStart < intervalMs) {
                speedSum += point.speed;
                distanceSum += point.distance;
                caloriesSum += point.calories;
                count++;
            } else {
                aggregatedPoints.add(createAggregatedPoint(intervalStart, speedSum, distanceSum, caloriesSum, count));
                intervalStart = point.timestamp;
                speedSum = point.speed;
                distanceSum = point.distance;
                caloriesSum = point.calories;
                count = 1;
            }
        }

        // Handle the last interval
        if (count > 0) {
            aggregatedPoints.add(createAggregatedPoint(intervalStart, speedSum, distanceSum, caloriesSum, count));
        }

        dataPoints = aggregatedPoints;
    }

    private DataPoint createAggregatedPoint(long timestamp, double speedSum, double distanceSum, double caloriesSum, int count) {
        return new DataPoint(
                timestamp,
                count > 0 ? speedSum / count : 0,
                distanceSum,
                caloriesSum
        );
    }

    public void autoAggregateData() {
        if (dataPoints.isEmpty()) return;
        if (dataPoints.size() < 10) return;

        long startTime = dataPoints.getFirst().timestamp;
        long endTime = dataPoints.getLast().timestamp;
        long duration = endTime - startTime;

        long intervalMs = calculateOptimalInterval(duration, dataPoints.size());
        aggregateData(intervalMs);
    }

    private long calculateOptimalInterval(long duration, int pointCount) {
        long targetPointCount = 1000; // Aim for about 1000 points after aggregation
        long optimalInterval = Math.max(duration / targetPointCount, 1000); // Ensure minimum 1-second interval

        for (int i = 0; i < niceIntervals.length - 1; i++) {
            if (optimalInterval <= niceIntervals[i]) {
                return niceIntervals[i];
            }
        }

        return niceIntervals[niceIntervals.length - 1]; // Return 1 hour if no smaller interval found
    }

    public void update(float speed, double triggerTimeHours) {
        // Now calculate average speed
        this.speedAvgQueue.add(speed);
        this.sumSpeed += speed;
        if (speedAvgQueue.size() > 2) {
            sumSpeed -= speedAvgQueue.poll();
        }

        float avgSpeed = 0;
        if (!speedAvgQueue.isEmpty()) {
            avgSpeed = sumSpeed / speedAvgQueue.size();
            avgSpeed = avgSpeed > 0 ? avgSpeed : 0;
        }

        // Round to last two decimal places
        avgSpeed = (float) (Math.round(avgSpeed * 100.0) / 100.0);

        double distance = avgSpeed * triggerTimeHours * 1000;
        double calories = ClientConfig.CONFIG.instance().calculateCalories(avgSpeed, triggerTimeHours);

        this.totalDistance += distance;
        this.totalCalories += calories;

        long timestamp = System.currentTimeMillis();

        DataPoint point = new DataPoint(timestamp, avgSpeed, distance, calories);
        boolean somethingChanged = (dataPoints.isEmpty() || point.speed != avgSpeed);
        dataPoints.add(point);

        // Notify listeners
        if (!somethingChanged) {
            for (Consumer<DataPoint> listener : onChangeListeners) {
                listener.accept(point);
            }
        } else {
            for (Consumer<DataPoint> listener : onNothingChangedListeners) {
                listener.accept(point);
            }
        }

        autoAggregateData();
    }

    public float getWheelRadius() {
        return wheelRadius;
    }

    public void setWheelRadius(float wheelRadius) {
        this.wheelRadius = wheelRadius;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public DataPoint getLatestDataPoint() {
        return dataPoints.isEmpty() ? null : dataPoints.getLast();
    }

}