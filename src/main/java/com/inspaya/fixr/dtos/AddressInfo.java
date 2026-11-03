package com.inspaya.fixr.dtos;

import com.inspaya.fixr.entities.location.City;
import com.inspaya.fixr.entities.location.Lga;
import com.inspaya.fixr.entities.location.State;

public record AddressInfo(
        State state,
        Lga lga,
        City city
){}