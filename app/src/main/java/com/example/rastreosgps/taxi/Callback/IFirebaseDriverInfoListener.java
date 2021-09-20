package com.example.rastreosgps.taxi.Callback;

import com.example.rastreosgps.taxi.Model.DriverGeoModel;

public interface IFirebaseDriverInfoListener {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
