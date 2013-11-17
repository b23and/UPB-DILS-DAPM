package com.example.savedisplay;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB) public class MainActivity extends Activity
{
	ArrayList<Data> data = null; //Data displayed on the ListView
	MyCustomAdapter dataAdapter = null;
	final String SEPARATOR = "~"; //The line separator used when writing in a file
	final int REQ_CODE_EDIT = 1;
	final String KEY_IDENTIFIER = "editedData";
	final String RESULT = "result";

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.d("ATag", "On create");
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Utils.activityContext = MainActivity.this;
		displayListView();
		invalidateOptionsMenu();
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
		int noOfCellsChecked = 0;
		
		for(int i = 0; i < data.size(); i++)
		{
			Data dataIterator = data.get(i);
			if(dataIterator.isSelected())
			{
				++noOfCellsChecked;
			}
		}
		
		Log.d("SomeTag", "Recreating the action bar||" + String.valueOf(noOfCellsChecked));
		
	    if (noOfCellsChecked == 0)
		{
	    	menu.getItem(1).setEnabled(false);
		    menu.getItem(2).setEnabled(false);
		}
	    else
	    if(noOfCellsChecked >= 2)
	    {
	    	menu.getItem(1).setEnabled(false);
	    }
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_new:
			{
				Intent intentToCreate = new Intent(this, EditActivity.class);
				startActivity(intentToCreate);
				
				break;
			}
			case R.id.action_edit:
			{
				String dataToEdit = null;
				int indexOfEdited = -1;
				for(int i = 0; i < data.size(); i++)
				{
					Data dataIterator = data.get(i);
					if(dataIterator.isSelected())
					{
						indexOfEdited = i;
						dataToEdit = dataIterator.getName();
					}
				}
				
				String[] dataAndIndex = {dataToEdit, String.valueOf(indexOfEdited)};
				
				if (dataToEdit != null)
				{
					Intent intentToEdit = new Intent(this, EditActivity.class);
					intentToEdit.putExtra(KEY_IDENTIFIER, dataAndIndex);
					startActivityForResult(intentToEdit, REQ_CODE_EDIT);
				}
				else
				{
					Toast.makeText(this, "Data could not be edited.", Toast.LENGTH_SHORT);
				}
				
				break;
			}
			case R.id.action_discard:
			{
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
				{
				    @SuppressWarnings("unchecked")
					@Override
				    public void onClick(DialogInterface dialog, int which) 
				    {
				        switch (which)
				        {
				        	case DialogInterface.BUTTON_POSITIVE:
				        	{
				        		Utils.deleteFile();
				        		for(int i = 0; i < data.size(); i++)
				        		{
				        			Data dataIterator = data.get(i);
				        			if(dataIterator.isSelected())
				        			{						
				        				data.remove(i);
				        				--i;
				        			}
				        			else
				        			{
				        				String encrypted = "";
				        				try 
				        				{
				        					encrypted = Utils.encrypt(Utils.SEED, dataIterator.getName());
				        				} 
				        				catch (Exception e) 
				        				{
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
				        				Utils.writeToFile(encrypted + SEPARATOR, Context.MODE_APPEND);
				        			}
				        		}			
				        		dataAdapter.dataArray = ((ArrayList<Data>)data.clone());
				        		dataAdapter.refreshAdapter();				        	

				        		break;
				        	}

				        	case DialogInterface.BUTTON_NEGATIVE:
				        		//No button clicked
				        		break;
				        }
				    }
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to delete the selected items?").setPositiveButton("Yes", dialogClickListener)
				    .setNegativeButton("No", dialogClickListener).show();
								
				break;
			}
			default:
				break;
		}
		
		return true;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null)
		{
			if (requestCode == REQ_CODE_EDIT)
			{
				String[] result = data.getStringArrayExtra(RESULT);
				Log.d("ON_ACTIVITY_RESULT", "The edited text is: " + result[0] + " and the index is: " + result[1]);
				Data editedData = new Data(result[0], false);
				this.data.set(Integer.parseInt(result[1]), editedData);

				Utils.deleteFile();
				for(int i = 0; i < this.data.size(); i++)
				{
					Data dataIterator = this.data.get(i);
					String encrypted = "";
					try 
					{
						encrypted = Utils.encrypt(Utils.SEED, dataIterator.getName());
						Utils.writeToFile(encrypted + SEPARATOR, Context.MODE_APPEND);
					} 
					catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}	
				dataAdapter.refreshAdapter();
			}
		}
	}

	private void displayListView() 
	{
		//Array list of countries
		data = new ArrayList<Data>();
				
		String stringFromFile = Utils.readFromFile();
		Log.d("MainActivity", "FILE READ: " + stringFromFile);
		
		if (stringFromFile != "")
		{
			String[] entries = stringFromFile.split(SEPARATOR);
				
			for (int i = 0; i < entries.length; i++)
			{
				String decryptedStringFromFile = "";
				try 
				{
					decryptedStringFromFile = Utils.decrypt(Utils.SEED, entries[i]);
				} 
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Data entry = new Data(decryptedStringFromFile, false);
				data.add(entry);
			}
			//create an ArrayAdaptar from the String Array
			dataAdapter = new MyCustomAdapter(this,
					R.layout.country_info, data);
			ListView listView = (ListView) findViewById(R.id.listView1);
			// Assign adapter to ListView
			listView.setAdapter(dataAdapter);
		}
		
	}
	
	//****************DEFINITION OF MyCustomAdapter private class*****************
	private class MyCustomAdapter extends ArrayAdapter<Data> 
	{

		private ArrayList<Data> dataArray;

		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Data> dataArray) 
		{
			super(context, textViewResourceId, dataArray);
			this.dataArray = new ArrayList<Data>();
			if (dataArray != null)
			{
				this.dataArray.addAll(dataArray);
			}
		}

		private class ViewHolder 
		{			
			CheckBox name;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ViewHolder holder = null;
			Log.v("ConvertView", String.valueOf(position));

			if (convertView == null) 
			{
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.country_info, null);

				holder = new ViewHolder();
				holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
				convertView.setTag(holder);

				holder.name.setOnClickListener(new View.OnClickListener() 
				{  
					public void onClick(View v) 
					{  						
						CheckBox cb = (CheckBox) v ;  
						Data data = (Data) cb.getTag();  
						data.setSelected(cb.isChecked());
						invalidateOptionsMenu();
					}  
				});  
			} 
			else 
			{
				holder = (ViewHolder) convertView.getTag();
			}

			Data data = dataArray.get(position);
			holder.name.setText(data.getName());
			holder.name.setChecked(data.isSelected());
			holder.name.setTag(data);

			return convertView;
		}

		public synchronized void refreshAdapter() 
		{
			ArrayList<Data> copyOfData = new ArrayList<Data>();
    		copyOfData.addAll(dataArray);
			dataArray.clear();
			dataArray.addAll(copyOfData);
			notifyDataSetChanged();
		}

	}
}
