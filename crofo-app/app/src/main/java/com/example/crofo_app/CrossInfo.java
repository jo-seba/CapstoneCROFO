package com.example.crofo_app;

import com.skt.Tmap.TMapPoint;

public class CrossInfo {
    private int crossID;                    // 교차로 ID
    private Crosswalk frontCrosswalk;       // 저 앞 횡단보도
    private Crosswalk rightCrosswalk;       // 오른쪽 횡단보도
    private Crosswalk leftCrosswalk;        // 왼쪽 횡단보도
    private Crosswalk backCrosswalk;        // 앞 횡단보도
    private double[] centerLocation;         // 교차로 위치 lat lon 순서
    private double[] crossLocation0;         // 교차로 위치 lat lon 순서
    private double[] crossLocation1;         // 1 0
    private double[] crossLocation2;         // 2 3
    private double[] crossLocation3;         //
    private double trueBearing;             // 방향 북쪽 0도, 360도 기준
    private double[] crosswalkLocation0 = new double[2];
    private double[] crosswalkLocation1 = new double[2];
    private double[] crosswalkLocation2 = new double[2];
    private double[] crosswalkLocation3 = new double[2];

    public CrossInfo(){
        frontCrosswalk = new Crosswalk();
        rightCrosswalk = new Crosswalk();
        leftCrosswalk = new Crosswalk();
        backCrosswalk = new Crosswalk();
        centerLocation = null;
        crossLocation0 = null;
        crossLocation1 = null;
        crossLocation2 = null;
        crossLocation3 = null;
        crosswalkLocation0 = new double[2];
        crosswalkLocation1 = new double[2];
        crosswalkLocation2 = new double[2];
        crosswalkLocation3 = new double[2];
        trueBearing = -1;
    }

    public int getCrossID() {
        return crossID;
    }

    public Crosswalk getFrontCrosswalk() {
        return frontCrosswalk;
    }

    public Crosswalk getRightCrosswalk() {
        return rightCrosswalk;
    }

    public Crosswalk getLeftCrosswalk() {
        return leftCrosswalk;
    }

    public Crosswalk getBackCrosswalk() {
        return backCrosswalk;
    }

    public double[] getCenterLocation() {
        return centerLocation;
    }

    public double[] getCrossLocation0() {
        return crossLocation0;
    }

    public double[] getCrossLocation1() {
        return crossLocation1;
    }

    public double[] getCrossLocation2() {
        return crossLocation2;
    }

    public double[] getCrossLocation3() {
        return crossLocation3;
    }

    public double getTrueBearing() {
        return trueBearing;
    }

    public void setCrossID(int cid) {
        this.crossID = cid;
    }

    public void setFrontCrosswalk(Crosswalk frontCrosswalk) {
        this.frontCrosswalk = frontCrosswalk;
    }

    public void setFrontCrosswalkLocation(double[] location){
        this.frontCrosswalk.setCrosswalkLocation(location);
    }

    public void setRightCrosswalk(Crosswalk rightCrosswalk) {
        this.rightCrosswalk = rightCrosswalk;
    }

    public void setLeftCrosswalk(Crosswalk leftCrosswalk) {
        this.leftCrosswalk = leftCrosswalk;
    }

    public void setBackCrosswalk(Crosswalk backCrosswalk) {
        this.backCrosswalk = backCrosswalk;
    }

    public void setCenterLocation(double[] centerLocation) {
        this.centerLocation = centerLocation;
    }

    public void setCrossLocation0(double[] crossLocation0) {
        this.crossLocation0 = crossLocation0;
    }

    public void setCrossLocation1(double[] crossLocation1) {
        this.crossLocation1 = crossLocation1;
    }

    public void setCrossLocation2(double[] crossLocation2) {
        this.crossLocation2 = crossLocation2;
    }

    public void setCrossLocation3(double[] crossLocation3) {
        this.crossLocation3 = crossLocation3;
    }

    public void setTrueBearing(double trueBearing) {
        this.trueBearing = trueBearing;
    }

    public double[] getCrosswalkLocation0() {
        return crosswalkLocation0;
    }

    public void setCrosswalkLocation0(double[] crosswalkLocation0) {
        this.crosswalkLocation0 = crosswalkLocation0;
    }

    public double[] getCrosswalkLocation1() {
        return crosswalkLocation1;
    }

    public void setCrosswalkLocation1(double[] crosswalkLocation1) {
        this.crosswalkLocation1 = crosswalkLocation1;
    }

    public double[] getCrosswalkLocation2() {
        return crosswalkLocation2;
    }

    public void setCrosswalkLocation2(double[] crosswalkLocation2) {
        this.crosswalkLocation2 = crosswalkLocation2;
    }

    public double[] getCrosswalkLocation3() {
        return crosswalkLocation3;
    }

    public void setCrosswalkLocation3(double[] crosswalkLocation3) {
        this.crosswalkLocation3 = crosswalkLocation3;
    }

}
