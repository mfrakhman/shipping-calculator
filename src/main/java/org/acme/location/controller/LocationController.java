package org.acme.location.controller;

import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.common.response.ApiResponse;
import org.acme.location.dto.CityDto;
import org.acme.location.dto.DistrictDto;
import org.acme.location.dto.ProvinceDto;
import org.acme.location.dto.SubdistrictDto;
import org.acme.location.service.LocationService;

import java.util.List;

@Blocking
@Path("/api/location")
@Produces(MediaType.APPLICATION_JSON)
public class LocationController {

    @Inject
    LocationService locationService;

    @GET
    @Path("/provinces")
    public ApiResponse<List<ProvinceDto>> getProvinces() {
        return ApiResponse.ok("Provinces retrieved", locationService.getProvinces());
    }

    @GET
    @Path("/cities")
    public ApiResponse<List<CityDto>> getCities(@QueryParam("provinceId") String provinceId) {
        return ApiResponse.ok("Cities retrieved", locationService.getCities(provinceId));
    }

    @GET
    @Path("/districts")
    public ApiResponse<List<DistrictDto>> getDistricts(@QueryParam("cityId") String cityId) {
        return ApiResponse.ok("Districts retrieved", locationService.getDistricts(cityId));
    }

    @GET
    @Path("/subdistricts")
    public ApiResponse<List<SubdistrictDto>> getSubdistricts(@QueryParam("districtId") String districtId) {
        return ApiResponse.ok("Subdistricts retrieved", locationService.getSubdistricts(districtId));
    }
}
