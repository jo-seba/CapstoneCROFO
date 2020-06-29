package com.example.crofo_app;

public class Car {
    private int[] carLocation;          // 차량 위치 // 상대좌표라 int로 일단 둠

    public Car(int[] cL){
        carLocation = cL;
    }

    public int[] getCarLocation(){
        return carLocation;
    }

    public void setCarLocation(int[] cL){
        carLocation = cL;
    }

}
