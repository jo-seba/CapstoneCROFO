package com.example.crofo_app;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FindCrossRequest extends AsyncTask<String, String, String> {
    private double lat;
    private double lon;
    private SafetyDrive safetyDrive;
    private Context context;
    public CrossFrame crossframe;
    private CrossSocket sock[];
    private CrossAlert crossAlert;
    private boolean isNavi = false;

    public FindCrossRequest (double[] location, SafetyDrive SD, Context ct, CrossSocket sockets[]) {
        lat = location[0];
        lon = location[1];
        safetyDrive = SD;
        context = ct;
        crossframe = new CrossFrame(context);
        sock = sockets;
        crossAlert = new CrossAlert(context);
    }

    public FindCrossRequest (double[] location, SafetyDrive SD, Context ct, CrossSocket sockets[], boolean isN) {
        lat = location[0];
        lon = location[1];
        safetyDrive = SD;
        context = ct;
        crossframe = new CrossFrame(context);
        sock = sockets;
        crossAlert = new CrossAlert(context);
        isNavi = isN;
    }

    protected String doInBackground(String... urls) {

        try {

            //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArr = new JSONArray();

            jsonObj.accumulate("lat", doubleToString(lat));
            jsonObj.accumulate("lon", doubleToString(lon));

            HttpURLConnection con = null;
            BufferedReader reader = null;
            try{
                URL url = new URL(urls[0]);

                //연결을 함
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");//POST방식으로 보냄
                con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                con.connect();

                //서버로 보내기위해서 스트림 만듬
                OutputStream outStream = con.getOutputStream();
                //버퍼를 생성하고 넣음
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(jsonObj.toString());
                writer.flush();
                writer.close();//버퍼를 받아줌

                //서버로 부터 데이터를 받음
                InputStream stream = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                String json = buffer.toString();//서버로 부터 받은 값

                return json;

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(con != null){
                    con.disconnect();
                }
                try {
                    if(reader != null){
                        reader.close();//버퍼를 닫아줌
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            JSONObject resultJson = new JSONObject(result);
            boolean res = resultJson.getBoolean("result");
            if (res) {
                JSONArray resultArr = resultJson.getJSONArray("arr");
                int cnt = resultArr.length();
                // =================CrossInfo 리스트 초기화=========================== //
                safetyDrive.clearList();
                for (int i = 0; i < cnt; i++) {
                    JSONObject jsonObj = resultArr.getJSONObject(i);

                    // =================CrossInfo 리스트 생성=========================== //
                    CrossInfo crossInfo = new CrossInfo();
                    crossInfo.setCrossID(jsonObj.getInt("id"));
                    double[] centerLocation = new double[2];
                    centerLocation[0] = jsonObj.getDouble("cent_x");
                    centerLocation[1] = jsonObj.getDouble("cent_y");
                    double[] CrossLocation0 = new double[2];
                    double[] CrossLocation1 = new double[2];
                    double[] CrossLocation2 = new double[2];
                    double[] CrossLocation3 = new double[2];
                    CrossLocation0[0] = jsonObj.getDouble("loc_x0");
                    CrossLocation0[1] = jsonObj.getDouble("loc_y0");
                    CrossLocation1[0] = jsonObj.getDouble("loc_x1");
                    CrossLocation1[1] = jsonObj.getDouble("loc_y1");
                    CrossLocation2[0] = jsonObj.getDouble("loc_x2");
                    CrossLocation2[1] = jsonObj.getDouble("loc_y2");
                    CrossLocation3[0] = jsonObj.getDouble("loc_x3");
                    CrossLocation3[1] = jsonObj.getDouble("loc_y3");
                    double[] crosswalkLocation0 = new double[2];
                    double[] crosswalkLocation1 = new double[2];
                    double[] crosswalkLocation2 = new double[2];
                    double[] crosswalkLocation3 = new double[2];
                    crosswalkLocation0[0] = jsonObj.getDouble("cen_x0");
                    crosswalkLocation0[1] = jsonObj.getDouble("cen_y0");

                    crosswalkLocation1[0] = jsonObj.getDouble("cen_x1");
                    crosswalkLocation1[1] = jsonObj.getDouble("cen_y1");

                    crosswalkLocation2[0] = jsonObj.getDouble("cen_x2");
                    crosswalkLocation2[1] = jsonObj.getDouble("cen_y2");

                    crosswalkLocation3[0] = jsonObj.getDouble("cen_x3");
                    crosswalkLocation3[1] = jsonObj.getDouble("cen_y3");

                    crossInfo.setCenterLocation(centerLocation);
                    crossInfo.setCrossLocation0(CrossLocation0);
                    crossInfo.setCrossLocation1(CrossLocation1);
                    crossInfo.setCrossLocation2(CrossLocation2);
                    crossInfo.setCrossLocation3(CrossLocation3);

                    crossInfo.setCrosswalkLocation0(crosswalkLocation0);
                    crossInfo.setCrosswalkLocation1(crosswalkLocation1);
                    crossInfo.setCrosswalkLocation2(crosswalkLocation2);
                    crossInfo.setCrosswalkLocation3(crosswalkLocation3);

                    safetyDrive.addList(crossInfo);
                }
                // =================ROI 체크=========================== //
                ArrayList<CrossInfo> roiList = safetyDrive.crossListInROI(lat, lon);
                // =================ROI 하나 고르기=========================== //
                if(roiList.size() > 0) {
                    CrossInfo roi = safetyDrive.ifHaveManyROI(safetyDrive.getCurrentBearing(), roiList, safetyDrive.getCurrentLocation());
                    safetyDrive.drawPolygon(roi.getCrossLocation0(), roi.getCrossLocation1(), roi.getCrossLocation2(), roi.getCrossLocation3());
                    // =================횡단보도 정보 요청=========================== //
                    System.out.println(" 교차로 내 횡단보도 정보를 요청합니다 " + roi.getCrossID());

                    // =================횡단보도 띄우기=========================== //

                    if(!safetyDrive.getCrossFrame().getIsInROI()){
                        double[] direction = decideDirection(roi, safetyDrive.getCurrentLocation());
                        System.out.println(" direction은 무엇인가 ? " + direction[0]);

                        safetyDrive.getCrossFrame().setInROI(true);
                        safetyDrive.getCrossFrame().initAllCrossFrame();

                        if(isNavi){
                            if(safetyDrive.isTurnRight(roi)) {
                                safetyDrive.getCrossFrame().showNaviCrossFrame();
                                for (int i = 0; i < 2; i++) {
                                    if (sock[i].isConnected()) {
                                        sock[i].disconnect();
                                    }
                                    System.out.println(" 인터섹션 아이디 " + roi.getCrossID());
                                    double[] crosswalkPoint = null;
                                    switch (i){
                                        case 0:
                                            crosswalkPoint = roi.getCrosswalkLocation1(); break;
                                        case 1:
                                            crosswalkPoint = roi.getCrosswalkLocation2(); break;
                                    }
                                    sock[i].setSocket(roi.getCrossID(), crosswalkPoint, roi, safetyDrive.getCrossFrame(), direction, crossAlert, i+1);
                                    sock[i].connect();
                                    sock[i].run();
                                }
                            }
                        }
                        else {
                            safetyDrive.getCrossFrame().showAllCrossFrame();
                            for (int i = 0; i < 4; i++) {
                                if (sock[i].isConnected()) {
                                    sock[i].disconnect();
                                }
                                System.out.println(" 인터섹션 아이디 " + roi.getCrossID());
                                double[] crosswalkPoint = null;
                                switch (i){
                                    case 0:
                                        crosswalkPoint = roi.getCrosswalkLocation0(); break;
                                    case 1:
                                        crosswalkPoint = roi.getCrosswalkLocation1(); break;
                                    case 2:
                                        crosswalkPoint = roi.getCrosswalkLocation2(); break;
                                    case 3:
                                        crosswalkPoint = roi.getCrosswalkLocation3(); break;
                                }
                                sock[i].setSocket(roi.getCrossID(), crosswalkPoint, roi, safetyDrive.getCrossFrame(), direction, crossAlert, i);
                                sock[i].connect();
                                sock[i].run();
                            }
                        }
                    }

                }
                else  {
                    safetyDrive.getCrossFrame().stop();
                    if(isNavi){
                        for (int i = 0; i < 2; i++) {
                            if (sock[i].isConnected()) {
                                sock[i].disconnect();
                            }
                        }
                        safetyDrive.deleteNaviCrosswalk();
                        crossAlert.setIsAlertFalse();
                    }
                    else {
                        for (int i = 0; i < 4; i++) {
                            if (sock[i].isConnected()) {
                                sock[i].disconnect();
                            }
                        }
                        safetyDrive.deleteCrosswalk();
                        crossAlert.setIsAlertFalse();
                    }

                    System.out.println(" 교차로 내 횡단보도 정보 요청 안해요 ");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }

    private String doubleToString(double dou) {
        return "" + dou;
    }

    public double[] decideDirection(CrossInfo crossInfo, double[] currentLocation){
        //  1 0
        //  2 3
        //
        // Front:0 // Right:1 // Back:2 // Left:3

        double[] direction = new double[2];
        double minDistance = getDistance(currentLocation, crossInfo.getCrosswalkLocation0());
        double distance = 0;
        direction = crossInfo.getCrosswalkLocation0();

        distance = getDistance(currentLocation, crossInfo.getCrosswalkLocation1());
        if(minDistance > distance){
            minDistance = distance;
            direction = crossInfo.getCrosswalkLocation1();
        }

        distance = getDistance(currentLocation, crossInfo.getCrosswalkLocation2());
        if(minDistance > distance){
            minDistance = distance;
            direction = crossInfo.getCrosswalkLocation2();
        }

        distance = getDistance(currentLocation, crossInfo.getCrosswalkLocation3());
        if(minDistance > distance){
            minDistance = distance;
            direction = crossInfo.getCrosswalkLocation3();
        }


        return direction;
    }

    public double getDistance(double[] point1, double[] point2){
        double distance = 0;
        double theta = 0;
        double lat1 = point1[0];
        double lon1 = point1[1];
        double lat2 = point2[0];
        double lon2 = point2[1];

        theta = lon1 - lon2;
        distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        distance = Math.acos(distance);
        distance = rad2deg(distance);

        distance = distance * 60 * 1.1515;
        distance = distance * 1.609344;    // 단위 mile 에서 km 변환.
        distance = distance * 1000.0;      // 단위  km 에서 m 로 변환

        return distance;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }

    private int findDirection(CrossInfo roi, double[] direction){
        if(roi.getCrosswalkLocation0() == direction){
            return 0;
        }
        if(roi.getCrosswalkLocation1() == direction){
            return 1;
        }
        if(roi.getCrosswalkLocation2() == direction){
            return 2;
        }
        if(roi.getCrosswalkLocation3() == direction){
            return 3;
        }
        return -1;
    }
}
