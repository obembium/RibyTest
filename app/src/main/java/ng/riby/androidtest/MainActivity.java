package ng.riby.androidtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.riby.androidtest.room.repository.NoteRepository;
import ng.riby.androidtest.utils.Delay;
import ng.riby.androidtest.utils.LocationClass;
import ng.riby.androidtest.utils.Message;
import ng.riby.androidtest.utils.PH;
import ng.riby.androidtest.utils.PulsatorLayout;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.action)
    ImageView action;
    @BindView(R.id.pulsator)
    PulsatorLayout pulsator;
    public static NoteRepository noteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setActionImage();
        noteRepository = new NoteRepository(getApplicationContext());
        checkAndRequestPermissions(getApplicationContext(), MainActivity.this);

    }
    public static boolean checkAndRequestPermissions(Context context, Activity activity) {
        int c1 = ContextCompat.checkSelfPermission(context, android.Manifest.permission.INTERNET);
        int c2 = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE);
        int c3 = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int c4 = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (c1 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.INTERNET);
        }
        if (c2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (c3 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (c4 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        PH.get().setBoolean(this,"Active",false);
    }

    private void setActionImage() {
        if(PH.get().getBoolean(this,"Active",false)) {
            action.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.stop));
            pulsator.start();
            startCordinating();
        }
        else {
            action.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.start));
            pulsator.stop();


        }

    }

    @OnClick(R.id.action)
    public void onViewClicked() {

        if(!PH.get().getBoolean(this,"Active",false)) {
            PH.get().setBoolean(this,"Active",true);
            setActionImage();
            startCordinating();
        }else {
            stopCordinating();
        }


    }

    private void stopCordinating() {
        PH.get().setBoolean(this,"Active",false);
        setActionImage();
        Intent intent=new Intent(this,MapActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        },1000);
    }

    private void startCordinating() {
        new CountDownTimer(5000, 5000)
        {
            public void onTick(long l) {}
            public void onFinish()
            {
                LocationClass.getLocation.get(MainActivity.this, MainActivity.this);
                boolean check=PH.get().getBoolean(MainActivity.this,"Active",false);
             if(check)
                 startCordinating();
            }
        }.start();
    }


}
