package com.kadmuffin.bikesarepain.server.interfaces;

import com.kadmuffin.bikesarepain.server.helper.BikeState;

public interface IForceComponent {
    float calculateForce(BikeState currentState);
}
