package com.modelcity.leisure.cityroutes.repository.model;

import com.modelcity.leisure.cityplaces.repository.model.CityPlace;

import com.modelcity.leisure.cityroutes.store.model.CityRoutePlaceView;
import jakarta.persistence.*;
import lombok.*;

/** Join entity preserving the order in which places appear within a route. */
@Entity
@Table(name = "city_route_places")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityRoutePlace implements CityRoutePlaceView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private CityRoute route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private CityPlace place;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}

