package com.example.savedisplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class EditActivity extends Activity 
{
	final String SEPARATOR = "~";
	final String KEY_IDENTIFIER = "editedData";
	final String RESULT = "result";
	String[] receivedData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		if (savedInstanceState == null)
		{
			Bundle extras = getIntent().getExtras();
			if (extras == null)
			{
				receivedData = null;
			}
			else
			{
				receivedData = extras.getStringArray(KEY_IDENTIFIER);
				EditText editedText = (EditText)findViewById(R.id.editInfo);
				editedText.setText(receivedData[0]);
			}
		}
		else
		{
			receivedData = (String[]) savedInstanceState.getSerializable(KEY_IDENTIFIER);
			EditText editedText = (EditText)findViewById(R.id.editInfo);
			editedText.setText(receivedData[0]);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}
	
	
	//********************BUTTON ACTIONS*************************
	
	public void cancel(View v)
	{
		setResult(RESULT_OK, null); 
		finish();
	}
	
	public void saveEntry(View v)
	{
		Utils.activityContext = EditActivity.this;
		if (receivedData == null)
		{
			receivedData = new String[2];
		}
		EditText editedText = (EditText)findViewById(R.id.editInfo);
		receivedData[0] = editedText.getText().toString();
		
		if (receivedData[1] != null)
		{
			Log.d("SAVE ENTRY", v.toString());
			Log.d("EditActivity", "Edited entry " + editedText.getText());

			Intent returnIntent = new Intent();
			returnIntent.putExtra(RESULT,receivedData);
			setResult(RESULT_OK,returnIntent);     
			finish();
		}
		else
		{
			String encrypted = "";
			try 
			{
				encrypted = Utils.encrypt(Utils.SEED, receivedData[0]);
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d("EditActivity", "Saved entry " + receivedData[0]);
			Log.d("EditActivity", "Saved entry " + encrypted);
			Utils.writeToFile(encrypted + SEPARATOR, Context.MODE_APPEND);
			finish();
		}
	}
}
