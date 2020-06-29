package com.example.crofo_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class CrossFrame {
    private Context context;
    public Dialog frontdlg = null;
    public Dialog backdlg = null;
    public Dialog leftdlg = null;
    public Dialog rightdlg = null;
    private CrossInfo roi;
    private ArrayList<ImageView> viewFrontList;
    private ArrayList<ImageView> viewRightList;
    private ArrayList<ImageView> viewBackList;
    private ArrayList<ImageView> viewLeftList;
    private boolean stop = false;
    private CrossSocket[] sock;

    private boolean isInROI = false;
    private boolean isShowingWaningFront = false, isShowingWaningBack = false, isShowingWaningLeft = false, isShowingWaningRight = false;
    private ImageView warningFrontImg, warningBackImg, warningRightImg, warningLeftImg;

    public CrossInfo getRoi() {
        return roi;
    }

    public void setRoi(CrossInfo roi) {
        this.roi = roi;
    }

    public CrossFrame(Context context) {
        this.context = context;
        roi = null;
        viewFrontList = new ArrayList<ImageView>();
        viewRightList = new ArrayList<ImageView>();
        viewBackList = new ArrayList<ImageView>();
        viewLeftList = new ArrayList<ImageView>();
        stop = false;
        isInROI = false;
        warningFrontImg = new ImageView(context);
        warningBackImg = new ImageView(context);
        warningLeftImg = new ImageView(context);
        warningRightImg = new ImageView(context);

        warningFrontImg.setImageResource(R.drawable.crosswalk_front_alert);
        warningBackImg.setImageResource(R.drawable.crosswalk_back_alert);
        warningLeftImg.setImageResource(R.drawable.crosswalk_left_alert);
        warningRightImg.setImageResource(R.drawable.crosswalk_right_alert);
    }

    public void stop(){
        isInROI = false;
    }


    // 호출할 다이얼로그 함수를 정의한다.
    public void callCrossFront() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        frontdlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        frontdlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        frontdlg.setContentView(R.layout.crossframefront);

        // 커스텀 다이얼로그 위치 조정
        LayoutParams params = frontdlg.getWindow().getAttributes();
        params.x = 400;
        params.y = 300;
        // params.width = 300;
        // params.height = 300;
        frontdlg.getWindow().setAttributes(params);
        frontdlg.getWindow().setGravity(Gravity.TOP | Gravity.LEFT);

        // 배경 어두워지는거 없애기
        frontdlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public void callCrossBack() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        backdlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        backdlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        backdlg.setContentView(R.layout.crossframeback);

        // 커스텀 다이얼로그 위치 조정
        LayoutParams params = backdlg.getWindow().getAttributes();
        params.x = 400;
        params.y = 800 + 300 + 600;
        // params.width = 300;
        // params.height = 300;
        backdlg.getWindow().setAttributes(params);
        backdlg.getWindow().setGravity(Gravity.TOP | Gravity.LEFT);

        // 배경 어두워지는거 없애기
        backdlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    public void callCrossLeft() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        leftdlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        leftdlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        leftdlg.setContentView(R.layout.crossframeleft);

        // 커스텀 다이얼로그 위치 조정
        LayoutParams params = leftdlg.getWindow().getAttributes();
        // params.x = 500;
        params.y = 300 + 600;
        // params.width = 300;
        // params.height = 300;
        leftdlg.getWindow().setAttributes(params);
        leftdlg.getWindow().setGravity(Gravity.TOP | Gravity.LEFT);

        // 배경 어두워지는거 없애기
        leftdlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    public void callCrossRight() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        rightdlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        rightdlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        rightdlg.setContentView(R.layout.crossframeright);

        // 커스텀 다이얼로그 위치 조정
        LayoutParams params = rightdlg.getWindow().getAttributes();
        params.x = 1150;
        params.y = 300 + 600;
        // params.width = 300;
        // params.height = 300;
        rightdlg.getWindow().setAttributes(params);
        rightdlg.getWindow().setGravity(Gravity.TOP | Gravity.LEFT);

        // 배경 어두워지는거 없애기
        rightdlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    public void initAllCrossFrame(){
        callCrossBack();
        callCrossFront();
        callCrossLeft();
        callCrossRight();
    }

    public void showAllCrossFrame(){

        frontdlg.show();
        backdlg.show();
        rightdlg.show();
        leftdlg.show();
    }


    public void refreshFrontFrame(Crosswalk roi){
        for(int i = 0;i<viewFrontList.size();i++){
            ((ViewManager)viewFrontList.get(i).getParent()).removeView(viewFrontList.get(i));
        }
        //((ViewManager)iv.getParent()).removeView(iv);
        viewFrontList.clear();
        if(isShowingWaningFront && warningFrontImg.getParent() != null){
            ((ViewManager)warningFrontImg.getParent()).removeView(warningFrontImg);
        }

        if(roi == null) {
            frontdlg.show();
            return;
        }

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 500;
        param.height = 300;
        frontdlg.addContentView(warningFrontImg, param);
        isShowingWaningFront = true;

        ArrayList<Pedestrian> pedestrianList;
        ArrayList<Car> carList;
        pedestrianList = roi.getPedestrianList();
        carList = roi.getCarList();

        for(int i=0;i<pedestrianList.size();i++){
            System.out.println(" front 에 찍히는 거 " + pedestrianList.get(i).getPedestrianLocation()[0]+ "  " + pedestrianList.get(i).getPedestrianLocation()[1]);
            addObjFront(pedestrianList.get(i).getPedestrianLocation()[0], pedestrianList.get(i).getPedestrianLocation()[1], 0,
                    pedestrianList.get(i).getPedestrianDirection());
        }
        for(int i=0;i<carList.size();i++){
            addObjFront(carList.get(i).getCarLocation()[0], carList.get(i).getCarLocation()[1], 1, -1);
        }
        frontdlg.show();
    }

    public void refreshRightFrame(Crosswalk roi){

        for(int i = 0;i<viewRightList.size();i++){
            ((ViewManager)viewRightList.get(i).getParent()).removeView(viewRightList.get(i));
        }
        //((ViewManager)iv.getParent()).removeView(iv);
        viewRightList.clear();
        if(isShowingWaningRight && warningRightImg.getParent() != null){
            ((ViewManager)warningRightImg.getParent()).removeView(warningRightImg);
        }

        if(roi == null) {
            rightdlg.show();
            return;
        }

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 300;
        param.height = 500;
        rightdlg.addContentView(warningRightImg, param);
        isShowingWaningRight = true;

        ArrayList<Pedestrian> pedestrianList;
        ArrayList<Car> carList;
        pedestrianList = roi.getPedestrianList();
        carList = roi.getCarList();
        for(int i=0;i<pedestrianList.size();i++){
            addObjRight(pedestrianList.get(i).getPedestrianLocation()[0], pedestrianList.get(i).getPedestrianLocation()[1], 0,
                    pedestrianList.get(i).getPedestrianDirection());
        }
        for(int i=0;i<carList.size();i++){
            addObjRight(carList.get(i).getCarLocation()[0], carList.get(i).getCarLocation()[1], 1, -1);
        }
        rightdlg.show();
    }

    public void refreshBackFrame(Crosswalk roi){

        for(int i = 0;i<viewBackList.size();i++){
            ((ViewManager)viewBackList.get(i).getParent()).removeView(viewBackList.get(i));
        }
        //((ViewManager)iv.getParent()).removeView(iv);
        viewBackList.clear();
        if(isShowingWaningBack && warningBackImg.getParent() != null){
            ((ViewManager)warningBackImg.getParent()).removeView(warningBackImg);
        }

        if(roi == null) {
            backdlg.show();
            return;
        }

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 500;
        param.height = 300;
        backdlg.addContentView(warningBackImg, param);
        isShowingWaningBack = true;

        ArrayList<Pedestrian> pedestrianList;
        ArrayList<Car> carList;
        pedestrianList = roi.getPedestrianList();
        carList = roi.getCarList();
        for(int i=0;i<pedestrianList.size();i++){
            addObjBack(pedestrianList.get(i).getPedestrianLocation()[0], pedestrianList.get(i).getPedestrianLocation()[1], 0,
                    pedestrianList.get(i).getPedestrianDirection());
        }
        for(int i=0;i<carList.size();i++){
            addObjBack(carList.get(i).getCarLocation()[0], carList.get(i).getCarLocation()[1], 1, -1);
        }
        backdlg.show();
    }

    public void refreshLeftFrame(Crosswalk roi){

        for(int i = 0;i<viewLeftList.size();i++){
            ((ViewManager)viewLeftList.get(i).getParent()).removeView(viewLeftList.get(i));
        }
        //((ViewManager)iv.getParent()).removeView(iv);
        viewLeftList.clear();
        if(isShowingWaningLeft && warningLeftImg.getParent() != null){
            ((ViewManager)warningLeftImg.getParent()).removeView(warningLeftImg);
        }

        if(roi == null) {
            leftdlg.show();
            return;
        }

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 300;
        param.height = 500;
        leftdlg.addContentView(warningLeftImg, param);
        isShowingWaningLeft = true;

        ArrayList<Pedestrian> pedestrianList;
        ArrayList<Car> carList;
        pedestrianList = roi.getPedestrianList();
        carList = roi.getCarList();
        for(int i=0;i<pedestrianList.size();i++){
            addObjLeft(pedestrianList.get(i).getPedestrianLocation()[0], pedestrianList.get(i).getPedestrianLocation()[1], 0,
                    pedestrianList.get(i).getPedestrianDirection());
        }
        for(int i=0;i<carList.size();i++){
            addObjLeft(carList.get(i).getCarLocation()[0], carList.get(i).getCarLocation()[1], 1, -1);
        }
        leftdlg.show();
    }

    public void showNaviCrossFrame(){
        rightdlg.show();
        backdlg.show();
    }

    public void deleteAllCrossFrame(){
        try {
            frontdlg.dismiss();
            backdlg.dismiss();
            rightdlg.dismiss();
            leftdlg.dismiss();
        } catch (Exception e){
            Log.e("dismiss error", String.valueOf(e));
        }

    }

    public void deleteNaviCrossFrame(){
        try {
            frontdlg.dismiss();
            backdlg.dismiss();
            rightdlg.dismiss();
            leftdlg.dismiss();
        } catch (Exception e){
            Log.e("dismiss error", String.valueOf(e));
        }

    }

    public void addObjFront(int left, int top, int obj, int direction){
        ImageView iv = new ImageView(context);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 60;
        param.height = 60;

        if(obj == 0){
            if(direction == 0){
                iv.setImageResource(R.drawable.person);
            }
            else{
                iv.setImageResource(R.drawable.person_direction);
                param.width = 100;
                param.height = 60;
                if(direction == 1){
                    iv.setRotation(-180.0f);
                }
            }
        }

        else{
            param.width = 90;
            param.height = 150;
            iv.setImageResource(R.drawable.car);
        }
        // 오브젝트 받아서 if로 나누면 댐


        param.setMargins(convertMargin500(left, param.width),convertMargin300(top, param.height),0,0);

        frontdlg.addContentView(iv, param);
        viewFrontList.add(iv);
    }

    public void addObjRight(int left, int top, int obj, int direction){
        ImageView iv = new ImageView(context);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 60;
        param.height = 60;

        if(obj == 0){
            if(direction == 0){
                iv.setImageResource(R.drawable.person);
            }
            else{
                iv.setImageResource(R.drawable.person_direction_2);
                param.width = 60;
                param.height = 100;
                if(direction == -1){
                    iv.setRotation(180.0f);
                }
            }
        }

        else{
            param.width = 150;
            param.height = 90;
            iv.setImageResource(R.drawable.car2);
        }

        // 오브젝트 받아서 if로 나누면 댐
        System.out.println(" 컨버트 " + convertMargin300(left, param.height) + " " + convertMargin500(top, param.width));
        param.setMargins(convertMargin300(left, param.width),convertMargin500(top, param.height),0,0);
        rightdlg.addContentView(iv, param);
        viewRightList.add(iv);
    }

    public void addObjBack(int left, int top, int obj, int direction){
        ImageView iv = new ImageView(context);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 60;
        param.height = 60;

        // 오브젝트 받아서 if로 나누면 댐
        if(obj == 0){
            if(direction == 0){
                iv.setImageResource(R.drawable.person);
            }
            else{
                iv.setImageResource(R.drawable.person_direction);
                param.width = 100;
                param.height = 60;
                if(direction == -1){
                    iv.setRotation(-180.0f);
                }
            }
        }

        else{
            param.width = 90;
            param.height = 150;
            iv.setImageResource(R.drawable.car);
        }

        param.setMargins(convertMargin500(left, param.width),convertMargin300(top, param.height),0,0);
        backdlg.addContentView(iv, param);
        viewBackList.add(iv);
    }

    public void addObjLeft(int left, int top, int obj, int direction){
        ImageView iv = new ImageView(context);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 60;
        param.height = 60;

        // 오브젝트 받아서 if로 나누면 댐
        if(obj == 0){
            if(direction == 0){
                iv.setImageResource(R.drawable.person);
            }
            else{
                iv.setImageResource(R.drawable.person_direction_2);
                param.width = 60;
                param.height = 100;
                if(direction == 1){
                    param.width = 90;
                    param.height = 150;
                    iv.setRotation(180.0f);
                }
            }
        }

        else{
            param.width = 150;
            param.height = 90;
            iv.setImageResource(R.drawable.car2);
        }
        param.setMargins(convertMargin300(left, param.width),convertMargin500(top, param.height),0,0);
        leftdlg.addContentView(iv, param);
        viewLeftList.add(iv);
    }

    public boolean getIsInROI() {
        return isInROI;
    }

    public void setInROI(boolean inROI) {
        isInROI = inROI;
    }

    public int convertMargin500(int margin, int param){
        // 500 : 500 - param = margin : convert
        int convert = 0;
        convert = (margin * (500 - param)) / 500;
        return convert;
    }

    public int convertMargin300(int margin, int param){
        // 300 : 300 - param = margin : convert
        int convert = 0;
        convert = (margin * (300 - param)) / 300;
        return convert;
    }

}
