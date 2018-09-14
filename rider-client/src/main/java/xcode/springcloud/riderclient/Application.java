package xcode.springcloud.riderclient;

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

        int numRiders = 2;
        Rider riders[] = Application.createRiders(numRiders);
        Trip[] trips = new Trip[numRiders];

        // Initialize rider locations
        for (int i = 0; i < numRiders; i++) {
            long riderId = riders[i].id;
            String createTripUrl = Application.getCreateTripUrl();

            HttpEntity<Trip> request = new HttpEntity<>(
                    new Trip(0,
                            0,
                            riderId,
                            GeoHashUtils.encode(Application.getRandomLatitude(), Application.getRandomLongitude()),
                            GeoHashUtils.encode(Application.getRandomLatitude(), Application.getRandomLongitude()),
                            0,
                            null,
                            null));
            trips[i] = restTemplate.postForObject(createTripUrl, request, Trip.class);
        }

        for (int count = 0; count < 100; count++) {
            for (int i = 0; i < numRiders; i++) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long tripId = trips[i].id;
                long riderId = riders[i].id;

                String tripUrl = Application.getGetTripUrl(tripId);
                Trip trip = restTemplate.getForObject(tripUrl, Trip.class);

                // If no matched driver
                if(trip.driverId == 0){
                    String checkTripUrl = Application.getCheckTripUrl(tripId);
                    restTemplate.postForObject(checkTripUrl, null, Trip.class);
                    logger.info("checked trip for rider " + riderId + " for " + count + " times");
                }else{
                    logger.info("No action for rider " + riderId + " for " + count + " times");
                }
            }
        }
    }

    private static Rider[] createRiders(int numRiders){

        Rider[] riders = new Rider[numRiders];

        for (int i = 0; i < numRiders; i++) {
            String createTripUrl = Application.getCreateRiderUrl();

            HttpEntity<Rider> request = new HttpEntity<>(
                    new Rider(
                            "Rider" + i,
                            "XCode",
                            "1 Hacker Way, Menlo Park, CA",
                            "123-456-7890",
                            "rider" + i + "@xcode.com",
                            "wechat"));

            RestTemplate restTemplate = new RestTemplate();
            riders[i] = restTemplate.postForObject(createTripUrl, request, Rider.class);
        }

        return riders;
    }

    private static String getLocationUrl(long driverId){
        return "http://localhost:8096/services/location/drivers/"
                + driverId + "/location";
    }

    private static String getCreateRiderUrl(){
        return "http://localhost:8096/services/account/riders/";
    }

    // Create trip via dispatch service
    private static String getCreateTripUrl(){
        return "http://localhost:8096/services/dispatch/trips/";
    }

    // Check trip via dispatch service
    private static String getCheckTripUrl(long tripId){
        return Application.getCreateTripUrl() + tripId + "/check";
    }

    // Access trip details via dispatch service
    private static String getGetTripUrl(long tripId){
        return Application.getCreateTripUrl() + tripId;
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