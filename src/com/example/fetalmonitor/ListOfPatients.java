package com.example.fetalmonitor;


import java.io.File;
import java.util.List;

import com.example.fetalmonitor.fragment.AlertDialogAddPatient;
import com.example.fetalmonitor.model.Patient;
import com.example.fetalmonitor.model.PatientListViewHolder;

import DatabaseHandler.DatabaseHandler;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListOfPatients extends FragmentActivity implements AlertDialogAddPatient.NoticeDialogListener{

	Button add;
	DatabaseHandler db;
	LinearLayout patientListLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_of_patients);
		
		patientListLayout = (LinearLayout) findViewById(R.id.patientList);
		getListStoredReports();
		addListenerOnButton();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_of_patients, menu);
		return true;
	}
	
	
	public void addListenerOnButton() 
    {

		add = (Button) findViewById(R.id.buttonNewPatient);
		
		add.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				Animation shake = AnimationUtils.loadAnimation(ListOfPatients.this, R.drawable.up_down);
				arg0.startAnimation(shake);
				
				AlertDialogAddPatient commentDialog = new AlertDialogAddPatient();
		        
		        commentDialog.show(getSupportFragmentManager(), "tag"); // or getFragmentManager() in API 11+
		        
			}
		});
    }
	
	public void getListStoredReports() 
    {
		
		//defaulText = (TextView)findViewById(R.id.default_TextView);
		db = new DatabaseHandler(this);

        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts.."); 
        List<Patient> patientsList = db.getAllContacts();       
         
        if (patientsList.size() != 0) 
        {
        	findViewById(R.id.emptyPatientList).setVisibility(View.GONE);
        	
        	for (Patient patient : patientsList) 
            {
        		View v = null;

				//Creates a holder from the custom holder class "ReportListViewHolder"
			    PatientListViewHolder holder = new PatientListViewHolder();
		
			    // First let's verify the convertView is not null
			    if (v == null) 
			    {
			        //This a new view we inflate the new layout
			        LayoutInflater inflater = (LayoutInflater) ListOfPatients.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			        v = inflater.inflate(R.layout.single_patient, null);
			        
			        // Now we can fill the layout with the right values
			        TextView titleView = (TextView) v.findViewById(R.id.nameTextView); 
			        Button deleteButton = (Button) v.findViewById(R.id.patientDeleteButton);
			        
			        //Assign the views to the holders
			        holder.name = titleView;
			        holder.deletePatientButton = deleteButton;
			        
			        v.setTag(holder);
			    }
			    else 
			        holder = (PatientListViewHolder) v.getTag();
		
			   
			    //This is were the information obtained from the API gets distribute to their proper views to be display on the Report List
		        holder.name.setText(patient.getName());

		        //Store the unSR.getPhotoPath() value to the submitReportButton with tag so its ClickListner can read it.
		        holder.deletePatientButton.setTag(patient);

		        /*This is the layout that holds the views for the Title,Distance,Thumbnail and Status view so in this way this
		        layout can be treated as a button to call the report profile, keeping it separated from the up-vote button.*/
		        holder.name.setTag(patient.getName());
		        holder.name.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View arg0)
					{
						Intent intent = new Intent(arg0.getContext(), HeartRateScreen.class);
						intent.putExtra("PatientName", arg0.getTag().toString());
			        	startActivity(intent);
					}
				
				});
		        
		        holder.deletePatientButton.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View arg0)
					{
						deleteReport((Patient)arg0.getTag());
					}
				
				});
	
		        patientListLayout.addView(v);
            }
        	//Removes the Layout containing the text "You dont have any reports stored" if the SQLite database is not empty.
        	
       }
        else
        {
        	findViewById(R.id.emptyPatientList).setVisibility(View.VISIBLE);
        	//findViewById(R.id.textNonStoredReports).setVisibility(View.VISIBLE);
        	//defaulText = (TextView)findViewById(R.id.default_TextView);
        	//Add the Layout containing the text back of "You dont have any reports stored" if the last item in the SQLite database gets deleted.
        	//defaulText.setVisibility(View.VISIBLE);
        }
	}
    
    public void deleteReport(Patient p)
    {
		
    	db.deleteUnSubmitedReport(p.getID());
    	patientListLayout.removeAllViews();
		getListStoredReports();
    }

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		patientListLayout.removeAllViews();
		getListStoredReports();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

}
