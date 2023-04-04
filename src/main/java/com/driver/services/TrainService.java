package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    PassengerRepository passengerRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train =new Train();
        train.setRoute(trainEntryDto.getStationRoute().toString());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
          Train train1=  trainRepository.save(train);


        return train1.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
           Train train= trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        if (train == null) {
            return 0;
        }
       String route= train.getRoute();
        int totalNumberofSeats=train.getNoOfSeats();
        List<Ticket> booking=train.getBookedTickets();

       return totalNumberofSeats-booking.size();
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();

        if(!train.getRoute().contains(station.toString())) {
            throw new Exception("Train is not passing from this station");
        }

        List<Ticket> bookedTicketList = train.getBookedTickets();

        int noOfPassengersBoarding = 0;
        for(Ticket ticket : bookedTicketList) {
            if(ticket.getFromStation().equals(station)) {
                noOfPassengersBoarding += ticket.getPassengersList().size();
            }
        }

        return noOfPassengersBoarding;

    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0


        // If there are no passengers travelling on the train, return 0
        Train train = trainRepository.findById(trainId).orElse(null);
        if (train == null) {
            throw new IllegalArgumentException("Train not found");
        }

        List<Ticket> tickets = train.getBookedTickets();

        // If there are no passengers travelling on the train, return 0
        if (tickets == null || tickets.isEmpty()) {
            return 0;
        }
        List<Passenger> passengers= tickets.get(0).getPassengersList();

        // Find the oldest passenger age by iterating over the list of booked tickets
        int oldestAge = passengers.get(0).getAge();
        for (Passenger s: passengers) {
//            Passenger passenger = s.getAge();
            if (s.getAge() > oldestAge) {
                oldestAge = s.getAge();
            }
        }

        return oldestAge;
    }




    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> trainList = trainRepository.findAll();

        List<Integer> trainIdList = new ArrayList<>();

        for(Train train : trainList) {
            //Filtering trains passing through the given station
            if(train.getRoute().contains(station.toString())) {
                //Filter the train by time of their arrival and departure at the given station

                //get the no. of station train have to travel to reach this station
                List<String> stationList = Arrays.asList(train.getRoute().split(","));
                long hourToBeAdded = stationList.indexOf(station.toString());

                //Arrival time of the train at given station
                LocalTime arrivalTimeOfTrain = train.getDepartureTime().plusHours(hourToBeAdded);
                if((startTime.isBefore(arrivalTimeOfTrain) || startTime.equals(arrivalTimeOfTrain)) && (endTime.isAfter(arrivalTimeOfTrain) || endTime.equals(arrivalTimeOfTrain))) {
                    trainIdList.add(train.getTrainId());
                }
            }
        }
        return trainIdList;
    }
    public int calculateFare(int trainId, String fromStation, String toStation) {
        Train train = trainRepository.findById(trainId).get();

        int fare = 0;

        List<String> route = Arrays.asList(train.getRoute().split(","));

        fare = 300 * (route.indexOf(toStation) - route.indexOf(fromStation));

        return fare;
    }

}
