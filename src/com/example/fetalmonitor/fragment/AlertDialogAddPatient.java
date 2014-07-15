package com.example.fetalmonitor.fragment;

import java.io.File;

import com.example.fetalmonitor.R;

import DatabaseHandler.DatabaseHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AlertDialogAddPatient extends DialogFragment 
{
	String name;
	Context context;
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	   
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
    	final Dialog commentDialog = new Dialog(getActivity());
    	commentDialog.setContentView(R.layout.alert_dialog_add_patient_layout);
    	commentDialog.setTitle("Adding a New Patient");
    	/*
    	LayoutParams lp = commentDialog.getWindow().getAttributes();
		
		Display display = getActivity().getWindowManager().getDefaultDisplay(); 
		
		if(display.getHeight() > display.getWidth())
		{
			lp.width = display.getWidth();
			lp.height = (int) (display.getHeight()/1.4);
		}
		else
		{
			lp.width = display.getWidth();
			lp.height = (int) (display.getHeight()/1.2);
		}
		commentDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) lp);
		*/
		
    	Button submit = (Button) commentDialog.findViewById(R.id.submitButton);
        Button cancel = (Button) commentDialog.findViewById(R.id.cancelButton);
        
        submit.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				EditText name_text_field = (EditText) commentDialog.findViewById(R.id.nameText);
				name = name_text_field.getText().toString();
				if(!name.equals(""))
				{
					submitPatient();
					mListener.onDialogPositiveClick(AlertDialogAddPatient.this);
					//Intent data = new Intent();
					//data.putExtra("commentWasSubmited", "commentWasSubmited");
					//context.getParent().setResult(1,data);
					commentDialog.dismiss();
				}
				else
				{
					Toast.makeText(context, "The Patient Name can't be empty", Toast.LENGTH_LONG).show();
				}
			}
		
		});
        
        cancel.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				commentDialog.dismiss();
				
			}
		
		});

        
        
        return commentDialog;
    }
    
    public void submitPatient()
	{
		DatabaseHandler db = new DatabaseHandler(context);
		
		// Inserting Contacts
	    Log.d("Insert: ", "Inserting .."); 
	    db.addUnSubmitedReport(name);
	    
		Toast.makeText(context, "New Patient Added to the List", Toast.LENGTH_LONG).show();
		//LinearLayout patientListLayout = (LinearLayout) getActivity().findViewById(R.id.patientList);
		//patientListLayout.removeAllViews();
	}
}