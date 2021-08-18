package com.example.rastreosgps.taxi;

import java.util.List;

public interface DirectionFinderListener {
    void onCreateViewHolder();

    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);

}
