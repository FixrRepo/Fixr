package com.inspaya.fixr.dtos;

import com.inspaya.fixr.entities.City;
import com.inspaya.fixr.entities.Lga;
import com.inspaya.fixr.entities.State;

public record AddressInfo(
        State state,
        Lga lga,
        City city
){}