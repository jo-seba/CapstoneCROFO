package com.example.crofo_app;

public class Pedestrian {
    private int[] pedestrianLocation;       // 보행자 위치 // 상대좌표라 int로 일단 둠
    private int pedestrianDirection;        // 보행자 방향 일단 int로 둠

    public Pedestrian(int[] pL, int pD) {
        pedestrianLocation = pL;
        pedestrianDirection = pD;
    }

    public int[] getPedestrianLocation() {
        return pedestrianLocation;
    }

    public int getPedestrianDirection() {
        return pedestrianDirection;
    }

    public void setPedestrianLocation(int[] pL){
        pedestrianLocation = pL;
    }

    public void setPedestrianDirection(int pD){
        pedestrianDirection = pD;
    }

}
