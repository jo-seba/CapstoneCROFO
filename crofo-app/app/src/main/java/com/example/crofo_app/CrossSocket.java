package com.example.crofo_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CrossSocket {
    private Socket socket;
    private String url;
    private int crosswalk;
    private double[] crosswalkLocation;
    private int intersection;
    private boolean isConnected;
    private boolean stop;
    private CrossInfo roi;
    public CrossFrame crossFrame;
    public double[] direction;
    private CrossAlert crossAlert;

    public CrossSocket(String url) {
        this.url = url;
        isConnected = false;
        stop = true;
    }

    public void setSocket(int intersection_id, double[] crosswalk_point, CrossInfo roi, CrossFrame cF, double[] direction_point, CrossAlert cA, int cw) {
        this.intersection = intersection_id;
        this.crosswalkLocation = crosswalk_point;
        isConnected = false;
        stop = true;
        this.roi = roi;
        crossFrame = cF;
        this.direction = direction_point;
        crossAlert = cA;
        crosswalk = cw;
    }

    public void run() {
        if(!isConnected) {
            System.out.println("Android-Node socket is not connected");
        } else {
            stop = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    System.out.println("android- Fuck" + stop);
                    while (!stop) {
                        try {
                            System.out.println("Thread is run now");
                            socket.emit("request", "hi");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("android- run exit" + cnt++);
                    }
                    System.out.println("android- run exit ");
                }
            }).start();
            socket.on("object", onMessageReceive);
        }
    }

    public void stop() {
        stop = true;
    }

    public void setKey(int intersection_id, int crosswalk_id) {
        if (isConnected) {
            System.out.println("Android-Node socket is connected: please disconnect first");
            return;
        }
        this.intersection = intersection_id;
        //this.crosswalk = crosswalk_id;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect() {
        try {
            socket = IO.socket(url);
            socket.connect();
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            isConnected = true;
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.accumulate("in", intersection);
                jsonObj.accumulate("x0", direction[0]);
                jsonObj.accumulate("y0", direction[1]);
                jsonObj.accumulate("x1", crosswalkLocation[0]);
                jsonObj.accumulate("y1", crosswalkLocation[1]);
                socket.emit("where", jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Android-Node socket is connected");
        }
    };

    private Emitter.Listener onMessageReceive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("소켓 받았어영", String.valueOf(System.currentTimeMillis()));
            JSONObject jsonObj = (JSONObject)args[0];
            System.out.println(jsonObj);
            try {
                JSONArray jsonArr = jsonObj.getJSONArray("arr");
                int cnt = jsonArr.length();
                // 횡단보도에 사람 없을 때
                if(cnt == 0) {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            switch (crosswalk){
                                case 0:
                                    crossFrame.refreshFrontFrame(null); break;
                                case 1:
                                    crossFrame.refreshRightFrame(null); break;
                                case 2:
                                    crossFrame.refreshBackFrame(null); break;
                                case 3:
                                    crossFrame.refreshLeftFrame(null); break;
                            }
                        }
                    }, 0);
                }

                switch (crosswalk){
                    case 0:
                        roi.getFrontCrosswalk().clearLists(); break;
                    case 1:
                        roi.getRightCrosswalk().clearLists(); break;
                    case 2:
                        roi.getBackCrosswalk().clearLists(); break;
                    case 3:
                        roi.getLeftCrosswalk().clearLists(); break;
                }



                for (int i = 0; i < cnt; i++) {
                    JSONObject json = jsonArr.getJSONObject(i);
                    //0 사람 1 차 2 bike 3 버스 4 트럭
                    int type = json.getInt("type");

                    System.out.println("컨벌트전 " + json.getInt("x") +  "  /  "  + json.getInt("y"));
                    int[] typeLocation = convertByDirection(json.getInt("x"), json.getInt("y"), crosswalk);
                    System.out.println("컨벌트후 " + typeLocation[0] + "/" + typeLocation[1]);



                    crossAlert.alertSound();
                    crossAlert.setIsAlertTrue();

                    // 사람일 때
                    if(type == 0){
                        int typeDirection = json.getInt("direction");
                        switch (crosswalk){
                            case 0:
                                roi.getFrontCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                            case 1:
                                roi.getRightCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                            case 2:
                                roi.getBackCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                            case 3:
                                roi.getLeftCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                        }
                    }

                    else if(type == 2){
                        int typeDirection = 0;
                        switch (crosswalk){
                            case 0:
                                roi.getFrontCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                            case 1:
                                roi.getRightCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                            case 2:
                                roi.getBackCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                            case 3:
                                roi.getLeftCrosswalk().addPedestrianList(new Pedestrian(typeLocation, typeDirection)); break;
                        }
                    }

                    // 차일 때
                    else{
                        switch (crosswalk){
                            case 0:
                                roi.getFrontCrosswalk().addCarList(new Car(typeLocation)); break;
                            case 1:
                                roi.getRightCrosswalk().addCarList(new Car(typeLocation)); break;
                            case 2:
                                roi.getBackCrosswalk().addCarList(new Car(typeLocation)); break;
                            case 3:
                                roi.getLeftCrosswalk().addCarList(new Car(typeLocation)); break;
                        }
                    }


                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            switch (crosswalk){
                                case 0:
                                    crossFrame.refreshFrontFrame(roi.getFrontCrosswalk()); break;
                                case 1:
                                    crossFrame.refreshRightFrame(roi.getRightCrosswalk()); break;
                                case 2:
                                    crossFrame.refreshBackFrame(roi.getBackCrosswalk()); break;
                                case 3:
                                    crossFrame.refreshLeftFrame(roi.getLeftCrosswalk()); break;
                            }
                        }
                    }, 0);


                    //sock.disconnect();


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
            System.out.println("Android-Node socket is disconnected");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
            System.out.println("Android-Node socket has Connection-Error");
        }
    };

    private Emitter.Listener onConnectTimeoutError = new Emitter.Listener() {
        public void call(Object... args) {
            isConnected = false;
            System.out.println("Android-Node socket has Connection-Timeout-Error");
        }
    };

    public void disconnect() {
        stop();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError);
        isConnected = false;
        System.out.println("Android-Node socket is disconnected");
    }

    public int[] convertByDirection(int x, int y, int crosswalk_id){
       int[] coordinates = new int[2];
       int convertX = 0, convertY = 0;
       switch (crosswalk_id){
           case 0:
               convertX = 500 - x;
               convertY = 300 - y;
               break;
           case 1:
               convertX = y;
               convertY = 500 - x;
               break;
           case 2:
               convertX = x;
               convertY = y;
               break;
           case 3:
               convertX = 300 - y;
               convertY = x;
               break;
       }
       coordinates[0] = convertX;
       coordinates[1] = convertY;
       return coordinates;
    }

}
