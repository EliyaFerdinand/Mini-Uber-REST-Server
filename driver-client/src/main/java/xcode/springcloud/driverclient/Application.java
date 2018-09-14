package xcode.springcloud.driverclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

public class Application {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Application.class);

        RestTemplate restTemplate = new RestTemplate();

        Random random = new Random();

        int numOfDrivers = 6;

        // Initialize driver locations
        for (int driverId = 1; driverId <= numOfDrivers; driverId++) {
            String locationUrl = Application.getLocationUrl(driverId);

            HttpEntity<Location> request = new HttpEntity<>(
                    new Location(driverId, getRandomLatitude(),
                            getRandomLongitude(), 0, 0));
            restTemplate.postForObject(locationUrl, request, Location.class);
        }

        for (int i = 0; i < 100; i++) {
            for (int driverId = 1; driverId <= numOfDrivers; driverId++) {

                String locationUrl = Application.getLocationUrl(driverId);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Get previous location
                Location location = restTemplate.getForObject(locationUrl, Location.class);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                location.setLatitude(latitude + Application.getRandomNumber(-0.01, 0.01));
                location.setLongitude(longitude + Application.getRandomNumber(-0.01, 0.01));

                if (location.getTripId() != 0) {
                    if (location.getStatus() == 1) {
                        // Get the trip
                        String tripUrl = Application.getTripUrl(location.getTripId());
                        Trip trip = restTemplate.getForObject(tripUrl, Trip.class);

                        // Accept the trip
                        location.setStatus(2);
                        HttpEntity<Location> request = new HttpEntity<>(location);
                        location = restTemplate.postForObject(locationUrl, request, Location.class);
                        logger.info("accepted trip " +
                                location.getTripId() + " and updated driver location table");

                        // update trip
                        trip.driverId = driverId;
                        HttpEntity<Trip> tripRequest = new HttpEntity<>(trip);
                        restTemplate.put(tripUrl, tripRequest, Trip.class);

                        logger.info("accepted trip " +
                                location.getTripId() + " and updated Trip table");
                    } else if (location.getStatus() == 2) {
                        // Randomly decide if the trip needs to completed
                        int randomNumber = random.nextInt(5);

                        // Complete the trip with 20% of chance
                        if (randomNumber == 0) {
                            // Get the trip
                            String tripUrl = Application.getTripUrl(location.getTripId());
                            Trip trip = restTemplate.getForObject(tripUrl, Trip.class);

                            // Update the trip table by set the trip status to 1 (completed)
                            trip.status = 1;
                            HttpEntity<Trip> tripRequest = new HttpEntity<>(trip);
                            restTemplate.put(tripUrl, tripRequest, Trip.class);

                            logger.info("completed trip " +
                                    trip.id + " and updated Trip table");

                            // Update the location table by wiping out tripId and status
                            location.setStatus(0);
                            location.setTripId(0);
                            HttpEntity<Location> request = new HttpEntity<>(location);
                            location = restTemplate.postForObject(locationUrl, request, Location.class);
                            logger.info("completed trip " +
                                    trip.id + " and updated driver location table");
                        }
                    }
                } else {
                    HttpEntity<Location> request = new HttpEntity<>(location);
                    location = restTemplate.postForObject(locationUrl, request, Location.class);
                }
                logger.info("updated driver " + driverId + " for " + i + " times");
            }
        }
    }

    // Access driver location via location service via zuul
    private static String getLocationUrl(long driverId){
        return "http://localhost:8096/services/location/drivers/"
                + driverId + "/location";
    }

    // Access trip details via dispatch service via zuul
    private static String getTripUrl(long tripId){
        return "http://localhost:8096/services/dispatch/trips/" + tripId;
    }

    private static double getRandomNumber(double min, double max) {
        return min + new Random().nextDouble() * (max - min);
    }

    private static double getRandomLatitude() {
        double lowerLatitudeLimit = -90;
        double upperLatitudeLimit = 90;
        return getRandomNumber(lowerLatitudeLimit, upperLatitudeLimit);
    }

    private static double getRandomLongitude() {
        double lowerLongitudeLimit = -180;
        double upperLongitudeLimit = 180;
        return getRandomNumber(lowerLongitudeLimit, upperLongitudeLimit);
    }
}