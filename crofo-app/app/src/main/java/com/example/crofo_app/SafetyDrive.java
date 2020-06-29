package com.example.crofo_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapPolygon;
import com.skt.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

public class SafetyDrive extends AsyncTask<TMapPoint, Void, Void> {
    private TimerTask gpsCheckTimerTask;
    private TMapPoint currentPoint;
    private double[] currentLocation = new double[2];
    private TMapView tMapView;      // View
    private TMapMarkerItem markerItemCurrent = new TMapMarkerItem();
    private ArrayList<CrossInfo> crossInfoList = new ArrayList<CrossInfo>();
    private boolean isInCross = false;
    private double currentBearing;
    private CrossFrame crossFrame;
    private TMapPoint startPoint;   // 출발지 좌표
    private TMapPoint endPoint;     // 목적지 좌표
    private  boolean isNavi = false;
    private ArrayList<double[]> coordinatesList = new ArrayList<double[]>();
    //private ArrayList<CrossInfo> serverRequestCrossList = new ArrayList<CrossInfo>();
    private ArrayList<String> turnTypeList = new ArrayList<String>();
    private ArrayList<String> descriptionList = new ArrayList<String>();

    private double[] recentLocation;

    Context context;
    CrossSocket sock[] = new CrossSocket[4];



    public SafetyDrive(TMapView tView, Context ct){
        super();
        tMapView = tView;
        context = ct;
        crossFrame = new CrossFrame(context);
//        crossFrame.initAllCrossFrame();
//        sock[0] = new CrossSocket("http://bic4907.diskstation.me:4446");
//        sock[0] = new CrossSocket("http://192.168.0.8:8080");
        for(int i = 0;i<4;i++){
            sock[i] = new CrossSocket("http://bic4907.diskstation.me:8081"); // 소켓 생성
//            sock[i] = new CrossSocket("http://192.168.0.7:8080"); // 소켓 생성
        }
    }

    public SafetyDrive(TMapPoint sPoint, TMapPoint ePoint, TMapView tView, Context ct) {
        this.startPoint = sPoint;
        this.endPoint = ePoint;
        this.tMapView = tView;
        this.context = ct;
        this.isNavi = true;
        crossFrame = new CrossFrame(context);
        for(int i = 0;i<2;i++){
//            sock[i] = new CrossSocket("http://192.168.0.7:8080"); // 소켓 생성
            sock[i] = new CrossSocket("http://bic4907.diskstation.me:8081"); // 소켓 생성

        }
    }

    protected void onPostExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected Void doInBackground(TMapPoint... tMapPoints){                       //실행부분
        tMapView.addMarkerItem("current",markerItemCurrent);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.mycar);
        bitmap = bitmap.createScaledBitmap(bitmap,100,100,true);
        markerItemCurrent.setIcon(bitmap);

        currentPoint = tMapView.getLocationPoint();
        currentLocation[0] = currentPoint.getLatitude();
        currentLocation[1] = currentPoint.getLongitude();

        if(isNavi){
            TMapData tMapData = new TMapData();
            TMapPolyLine tMapPolyLine = null;
            try {
                TMapMarkerItem startMarker = new TMapMarkerItem();
                TMapMarkerItem endMarker = new TMapMarkerItem();

                tMapPolyLine = tMapData.findPathData(startPoint, endPoint);             //길찾기
                tMapPolyLine.setLineColor(Color.GREEN);                                  //선 색
                tMapPolyLine.setLineWidth(2);                                           //선 굵기

                tMapView.addTMapPolyLine("Line123", tMapPolyLine);                  //맵에 추가

                // 마커의 좌표 지정
                startMarker.setTMapPoint(startPoint);
                endMarker.setTMapPoint(endPoint);

                // 마커의 타이틀 지정
                startMarker.setName("StartPoint");
                endMarker.setName("EndPoint");

                // 지도에 마커 추가
                tMapView.addMarkerItem("current", markerItemCurrent);
                tMapView.addMarkerItem("StartPoint", startMarker);
                tMapView.addMarkerItem("EndPoint", endMarker);

                // 화면 중심 시작 지점으로 설정
                tMapView.setCenterPoint(startPoint.getLongitude(), startPoint.getLatitude());

                // 화면 최대 확대
                tMapView.setZoomLevel(18);

                // 나침반 모드로 변경
                //tMapView.setCompassMode(true);

                tMapData.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, startPoint, endPoint, new TMapData.FindPathDataAllListenerCallback() {
                    @Override
                    public void onFindPathDataAll(Document document) {
                        Element root = document.getDocumentElement();
                        NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                        for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                            NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                            for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
                                if( nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:turnType") ) {
                                    turnTypeList.add(nodeListPlacemarkItem.item(j).getTextContent().trim());
                                    Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim() );
                                }
                                if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                                    descriptionList.add(nodeListPlacemarkItem.item(j).getTextContent().trim());
                                    Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim() );
                                }
                            }
                        }

                        NodeList nodeListPoint = root.getElementsByTagName("Point");
                        for( int i=0; i<nodeListPoint.getLength(); i++ ) {
                            NodeList nodeListPointItem = nodeListPoint.item(i).getChildNodes();
                            for( int j=0; j<nodeListPointItem.getLength(); j++ ) {
                                if( nodeListPointItem.item(j).getNodeName().equals("coordinates") ) {

                                    Log.d("debug", nodeListPointItem.item(j).getTextContent().trim() );

                                    String xy  = nodeListPointItem.item(j).getTextContent().trim();

                                    String [] splitXY = xy.split(",");
                                    TMapPoint point = new TMapPoint(Double.parseDouble(splitXY[1]), Double.parseDouble(splitXY[0]));
                                    double[] pointDouble = {Double.parseDouble(splitXY[1]), Double.parseDouble(splitXY[0])};
                                    coordinatesList.add(pointDouble);
                                    TMapMarkerItem Marker = new TMapMarkerItem();
                                    Marker.setTMapPoint(point);
                                    tMapView.addMarkerItem("asd" + point, Marker);
                                }
                            }
                        }

                        for(int i = 0; i < coordinatesList.size(); i++){
                            double trueBearing = 0;
                            System.out.println("좌표 : " + coordinatesList.get(i)[0] + "    " + coordinatesList.get(i)[1]);
                            System.out.println("TurnType : " + turnTypeList.get(i));
                            if(i==0) continue;
                            trueBearing = getTrueBearing(coordinatesList.get(i), coordinatesList.get(i-1));
                            System.out.println("교차로 진입 방향 : " + "  " + trueBearing);

                            //serverRequestCrossList.add(new CrossInfo(coordinatesList.get(i), trueBearing));
                        }

                    }
                });
            }
            catch (Exception e){e.printStackTrace();}
        }

        // 현재 위치 타이머로 0.5초마다 계속 얻기, 업데이트
        //tMapView.setTrackingMode(true);
        initTimerTask();
        Timer gpsCheckTimer = new Timer();
        gpsCheckTimer.schedule(gpsCheckTimerTask, 0, 500);

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    public void initTimerTask(){
        gpsCheckTimerTask = new TimerTask(){
            @Override
            public void run(){
                // 타이머로 할 일
                // 현재 위치 가져오기
                Log.d("현재위치", String.valueOf(tMapView.getLocationPoint()));
                // 최근 위치 저장
                recentLocation = currentLocation;
                currentPoint = tMapView.getLocationPoint();
                currentLocation[0] = currentPoint.getLatitude();
                currentLocation[1] = currentPoint.getLongitude();
                tMapView.setCenterPoint(currentPoint.getLongitude(), currentPoint.getLatitude());
                markerItemCurrent.setTMapPoint(currentPoint);
                currentBearing = getTrueBearing(recentLocation, currentLocation);

                if(isNavi){
//                    new FindCrossRequest(currentLocation, SafetyDrive.this, context, sock, true).execute("http://192.168.0.7:8080/app/cross/find");
                    new FindCrossRequest(currentLocation, SafetyDrive.this, context, sock, true).execute("http://bic4907.diskstation.me:8081/app/cross/find");
                }
                else {
                    new FindCrossRequest(currentLocation, SafetyDrive.this, context, sock).execute("http://bic4907.diskstation.me:8081/app/cross/find"); // 처음에 경로 찾고 교차로 목록 이렇게 보내면 됨.
//                    new FindCrossRequest(currentLocation, SafetyDrive.this, context, sock).execute("http://192.168.0.7:8080/app/cross/find"); // 처음에 경로 찾고 교차로 목록 이렇게 보내면 됨.
                }
                System.out.println("보냇어용");

            }
        };
    }

    public double getTrueBearing(double[] point1, double[] point2){
        // point[0] = lat , point[1] = lon

        double[] point1_radian = new double[2];
        double[] point2_radian = new double[2];

        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        point1_radian[0] = point1[0] * (3.141592 / 180);        //lat
        point1_radian[1] = point1[1] * (3.141592 / 180);        //lon

        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        point2_radian[0] = point2[0] * (3.141592 / 180);        //lat
        point2_radian[1] = point2[1] * (3.141592 / 180);        //lon

        // radian distance

        double radian_distance = 0;

        radian_distance = Math.acos(Math.sin(point1_radian[0]) * Math.sin(point2_radian[0]) + Math.cos(point1_radian[0]) * Math.cos(point2_radian[0]) * Math.cos(point1_radian[1] - point2_radian[1]));



        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.

        double radian_bearing = Math.acos((Math.sin(point2_radian[0]) - Math.sin(point1_radian[0]) * Math.cos(radian_distance)) / (Math.cos(point1_radian[0]) * Math.sin(radian_distance)));
        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.

        double true_bearing = 0;

        if (Math.sin(point2_radian[1] - point1_radian[1]) < 0)

        {

            true_bearing = radian_bearing * (180 / 3.141592);

            true_bearing = 360 - true_bearing;

        }

        else

        {

            true_bearing = radian_bearing * (180 / 3.141592);

        }

        return true_bearing;

    }

    public ArrayList<double[]> makeExtensionPolygon(CrossInfo crossInfo){
        // 위도 0.0009도 = 100m
        // 경도 0.001126도 = 100m
        ArrayList<double[]> List = new ArrayList<double[]>();
        double[] ext0 = new double[2];
        double[] ext1 = new double[2];
        double[] ext2 = new double[2];
        double[] ext3 = new double[2];
        ext0[0] = crossInfo.getCrossLocation0()[0];// + 0.0009;
        ext0[1] = crossInfo.getCrossLocation0()[1]; //+ 0.001126;
        List.add(ext0);
        ext1[0] = crossInfo.getCrossLocation1()[0]; // - 0.0009;
        ext1[1] = crossInfo.getCrossLocation1()[1]; //+ 0.001126;
        List.add(ext1);
        ext2[0] = crossInfo.getCrossLocation2()[0];// - 0.0009;
        ext2[1] = crossInfo.getCrossLocation2()[1]; //- 0.001126;
        List.add(ext2);
        ext3[0] = crossInfo.getCrossLocation3()[0];// + 0.0009;
        ext3[1] = crossInfo.getCrossLocation3()[1]; //- 0.001126;
        List.add(ext3);
        //drawPolygon(ext0, ext1, ext2, ext3);
        return List;
    }

    // ROI 안에 들어온 교차로 리스트 return
    public ArrayList<CrossInfo> crossListInROI(double curLat, double curLon){
        double[] currentLocation = new double[2];
        currentLocation[0] = curLat;
        currentLocation[1] = curLon;
        ArrayList <CrossInfo> List = new ArrayList<CrossInfo>();
        for(int i = 0;i<crossInfoList.size();i++){
            if(isInPolygon(makeExtensionPolygon(crossInfoList.get(i)), currentLocation)){
                List.add(crossInfoList.get(i));
            }
        }

        if(List.size() == 0) isInCross = false;
        else isInCross = true;

        return List;
    }

    // ROI 안에 들어왔는지
    public boolean isInPolygon(ArrayList<double[]> roi, double[] point){
        //crosses는 점q와 오른쪽 반직선과 다각형과의 교점의 개수
        int crosses = 0;

        for(int i = 0 ; i < 4 ; i++){
            int j = (i+1)%4;
            //점 B가 선분 (p[i], p[j])의 y좌표 사이에 있음
            if((roi.get(i)[1] > point[1]) != (roi.get(j)[1] > point[1])){
                //atX는 점 B를 지나는 수평선과 선분 (p[i], p[j])의 교점
                double atX = (roi.get(j)[0] - roi.get(i)[0]) * (point[1] - roi.get(i)[1]) / (roi.get(j)[1] - roi.get(i)[1]) + roi.get(i)[0];
                //atX가 오른쪽 반직선과의 교점이 맞으면 교점의 개수를 증가시킨다.
                if(point[0] < atX)
                    crosses++;
            }
        }
        return crosses % 2 > 0;
    }

    // 2개 이상이면 현재 차량 기준으로 필터링
    public CrossInfo ifHaveManyROI(double trueBearing, ArrayList<CrossInfo> roiList, double[] curLocation){
        if(roiList.size() == 1){
            return roiList.get(0);
        }

        int index = 0;
        double min = 360;
        for(int i = 0;i<roiList.size();i++){
            double diff = Math.abs(getTrueBearing(curLocation, roiList.get(i).getCenterLocation()) - trueBearing);
            if(min > diff){
                index = i;
                min = diff;
            }
        }
        return roiList.get(index);
    }

    public void clearList(){
        crossInfoList.clear();
    }

    public void addList(CrossInfo crossInfo){
        crossInfoList.add(crossInfo);
    }

    public void deleteCrosswalk(){
        crossFrame.deleteAllCrossFrame();
    }

    public void deleteNaviCrosswalk(){
        crossFrame.deleteNaviCrossFrame();
    }

    public double getCurrentBearing() {
        return currentBearing;
    }

    public double[] getCurrentLocation() {
        return currentLocation;
    }

    public boolean getIsInCross(){
        return isInCross;
    }

    public void drawPolygon(double[] ext0, double[] ext1, double[] ext2, double[] ext3){
        TMapPolygon tMapPolygon = new TMapPolygon();
        tMapPolygon.setLineColor(Color.BLUE);
        tMapPolygon.setPolygonWidth(2);
        tMapPolygon.setAreaColor(Color.GRAY);
        tMapPolygon.setAreaAlpha(100);
        tMapPolygon.addPolygonPoint( new TMapPoint(ext0[0], ext0[1]));
        tMapPolygon.addPolygonPoint( new TMapPoint(ext1[0], ext1[1]));
        tMapPolygon.addPolygonPoint( new TMapPoint(ext2[0], ext2[1]));
        tMapPolygon.addPolygonPoint( new TMapPoint(ext3[0], ext3[1]));
        tMapView.addTMapPolygon("Line1", tMapPolygon);
    }

    public CrossFrame getCrossFrame(){ return crossFrame; }

    public boolean isTurnRight(CrossInfo roi){
        ArrayList<double[]> points = new ArrayList<>();
        points.add(roi.getCrossLocation0());
        points.add(roi.getCrossLocation1());
        points.add(roi.getCrossLocation2());
        points.add(roi.getCrossLocation3());

        for(int i = 0; i < coordinatesList.size(); i++){
            // - 13: 우회전
            // - 18: 2시 방향 우회전
            // - 19: 4시 방향 우회전
            if(turnTypeList.get(i).equals("13") || turnTypeList.get(i).equals("18") || turnTypeList.get(i).equals("19")){
                if(isInPolygon(points, coordinatesList.get(i))){
                    return true;
                }
            }
        }

        return false;
    }

}
