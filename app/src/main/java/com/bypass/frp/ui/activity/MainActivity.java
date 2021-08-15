package com.bypass.frp.ui.activity;

import android.os.Bundle;
import com.bypass.frp.R;
import com.bypass.frp.common.activity.BaseActivity;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.content.Context;

public class MainActivity extends BaseActivity { 


	Button button_devop;
	Button button_sett;
	


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		

	
		button_devop = (Button) findViewById(R.id.btn_devop);
		button_devop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				call_developer(v.getContext());
			}
			
		});


		button_sett = (Button) findViewById(R.id.btn_set);
		button_sett.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					call_sett(v.getContext());
				}

			});
		
        
    }
	

	public void call_developer(Context c) {
		Intent settings_intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
		settings_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.startActivity(settings_intent);
	}
  

	public void call_sett(Context c) {
		Intent settings_intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		settings_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.startActivity(settings_intent);
	}

}
