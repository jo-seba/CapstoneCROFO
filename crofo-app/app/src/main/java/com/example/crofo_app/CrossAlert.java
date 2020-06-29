package com.example.crofo_app;

import android.content.Context;
import android.media.Image;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.speech.tts.TextToSpeech.ERROR;

public class CrossAlert{
    private String alertSound = "전방 교차로에 보행자 및 차량이 있습니다. 주의해주세요.";
    private TextToSpeech tts;
    private Context context;
    private boolean isAlert = false;

    public CrossAlert(Context ct){
        context = ct;
        isAlert = false;
    }

    public boolean getIsAlert(){
        return isAlert;
    }

    public void setIsAlertTrue(){
        isAlert = true;
    }
    public void setIsAlertFalse(){
        isAlert = false;
    }

    public void alertSound(){
        if(isAlert) return;

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    tts.speak(alertSound,TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

}
