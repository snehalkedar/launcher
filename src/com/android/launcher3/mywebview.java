package com.android.launcher3;

import android.webkit.WebView;
import android.view.KeyEvent;
import 	android.content.Context;
import android.webkit.WebSettings.PluginState;
public class mywebview extends WebView {


	public mywebview(Context context){
		super(context);
		this.getSettings().setPluginState(PluginState.ON);
		this.getSettings().setAppCacheEnabled(true);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event){
		int valKey = 0;
		System.out.println("Web KEY:"); 
		System.out.println(keyCode);    

		switch(keyCode){
		//UP
		case 50:
		case 19:
			valKey = 19;
			break;
			//DOWN
		case 83:
		case 20:
			valKey = 20;        
			break;
			//LEFT
		case 81:
		case 21:
			valKey = 21;
			break;
			//RIGHT
		case 69:
		case 22:
			valKey = 22;    
			break;

                case 4:
                       this.goBack();   
			 System.out.println("Techm Web KEY: "+"case 4");     
                       return super.onKeyDown(4, new KeyEvent(KeyEvent.ACTION_DOWN, valKey));
		}

		if (valKey!=0)
		{
			//new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SHIFT_LEFT);
			KeyEvent event1 = new KeyEvent(KeyEvent.ACTION_DOWN, valKey);

			System.out.println(event1.getKeyCode());    

			return super.onKeyDown(38, event1);
		}
		else
		{
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                               System.out.println("Techm Web KEY: Enter");
                               KeyEvent event1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER);
			       return super.onKeyDown(KeyEvent.ACTION_DOWN, event1);
                        }
			return super.onKeyDown(keyCode, event);
		}


	}


}
