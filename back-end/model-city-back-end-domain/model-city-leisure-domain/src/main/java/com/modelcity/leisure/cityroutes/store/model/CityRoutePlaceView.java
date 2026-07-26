package com.modelcity.leisure.cityroutes.store.model;

import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;

/** Read-only view of a route-place join, exposed by the persistence adapter. */
public interface CityRoutePlaceView {
    CityPlaceView getPlace();
    int getSortOrder();
}
