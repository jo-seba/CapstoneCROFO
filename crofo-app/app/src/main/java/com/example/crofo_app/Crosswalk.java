package com.example.crofo_app;

import com.skt.Tmap.TMapPoint;

import java.util.ArrayList;

public class Crosswalk {
    private ArrayList<Pedestrian> pedestrianList;       // 보행자 리스트
    private ArrayList<Car> carList;                     // 차량 리스트
    private double[] crosswalkLocation;                // 횡단보도 위치 ( 필요하려나? ) lat lon 순서

    public Crosswalk(){
        pedestrianList = new ArrayList<Pedestrian>();
        carList = new ArrayList<Car>();
        crosswalkLocation = new double[2];
    }

    public Crosswalk(ArrayList<Pedestrian> pL, ArrayList<Car> cL, double[] cwL){
        pedestrianList = pL;
        carList = cL;
        crosswalkLocation = cwL;
    }

    public ArrayList<Pedestrian> getPedestrianList(){
        return pedestrianList;
    }

    public ArrayList<Car> getCarList(){
        return carList;
    }

    public double[] getCrosswalkLocation(){
        return crosswalkLocation;
    }

    public void setPedestrianList(ArrayList<Pedestrian> pL){
        pedestrianList = pL;
    }

    public void setCarList(ArrayList<Car> cL){
        carList = cL;
    }

    public void setCrosswalkLocation(double[] cwL){
        crosswalkLocation = cwL;
    }

    public void addPedestrianList(Pedestrian p){
        pedestrianList.add(p);
    }

    public void addCarList(Car c){
        carList.add(c);
    }

    public void clearLists(){
        pedestrianList.clear();
        carList.clear();
    }
}
