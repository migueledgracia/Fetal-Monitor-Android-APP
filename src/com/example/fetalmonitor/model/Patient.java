package com.example.fetalmonitor.model;

public class Patient 
{	     
	    //private variables
		int id;
	    String name;
	    
	    // Empty constructor
	    public  Patient ()
	    {
	         
	    }
	    
	    // constructor
	    public Patient(String name){
	        this.name = name;
	    }
	    
	    // constructor
	    public Patient(int id, String name){
	        this.id = id;
	        this.name = name;
	    }
	    
	    // getting Patient ID
	    public int getID(){
	        return this.id;
	    }
	     
	    // setting Patient ID
	    public void setID(int id){
	        this.id = id;
	    }
	    
	    // getting Patient Name
	    public String getName(){
	        return this.name;
	    }
	     
	    // setting Patient Name
	    public void setName(String title){
	        this.name = title;
	    }
	    
	}