package com.inspaya.fixr.services;


import com.inspaya.fixr.dtos.AddressInfo;
import com.inspaya.fixr.dtos.LocationDto;
import com.inspaya.fixr.entities.City;
import com.inspaya.fixr.entities.Lga;
import com.inspaya.fixr.entities.State;
import com.inspaya.fixr.repositories.CityRepo;
import com.inspaya.fixr.repositories.LgaRepo;
import com.inspaya.fixr.repositories.StateRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final StateRepo stateRepo;
    private final LgaRepo lgaRepo;
    private final CityRepo cityRepo;

     public AddressInfo createLocation(@Valid LocationDto locationDto){
        State state = createOrReturnState(locationDto.state());
        Lga lga = createOrReturnLga(locationDto.lga(), state);
        City city = null;
        if(locationDto.city() != null) {
            city = createOrReturnCity(locationDto.city(), lga);
        }

        return new AddressInfo(state, lga, city);
    }

    public State createOrReturnState(String stateName){
        Optional<State> optionalState = stateRepo.findByName(stateName);

        if(optionalState.isPresent())
            return optionalState.get();

        State state = new State(stateName);

        return stateRepo.save(state);
    }

    public State getStateByName(String name){
         return stateRepo.findByName(name).orElseGet(()->null);
    }

    public Lga createOrReturnLga(String lgaName, State state){
        Optional<Lga> optionalLga = lgaRepo.findByNameAndState(lgaName, state);

        if(optionalLga.isPresent())
            return optionalLga.get();

        Lga lga = new Lga(lgaName, state);

        return lgaRepo.save(lga);
    }

    public Lga getLgaByName(String name, State state){
        return lgaRepo.findByNameAndState(name, state).orElseGet(()->null);
    }

    public City createOrReturnCity(String cityName, Lga lga){

        Optional<City> optionalCity = cityRepo.findByNameAndLga(cityName, lga);

        if(optionalCity.isPresent())
            return optionalCity.get();

        City city = new City(cityName, lga);
        return cityRepo.save(city);
    }
}
