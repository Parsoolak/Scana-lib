package co.scalium.scana;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import co.scalium.scabase.ScaBase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScaBase.init(this).setEvent("Install");

    }
}
