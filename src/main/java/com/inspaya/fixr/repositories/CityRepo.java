package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.location.City;
import com.inspaya.fixr.entities.location.Lga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepo extends JpaRepository<City, Long> {
    Optional<City> findByNameAndLga(String cityName, Lga lga);
}
