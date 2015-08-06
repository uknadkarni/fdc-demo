package com.pivotal.example.xd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Order implements Serializable {

	private String transactionId;
	private String creditCardType;
	private int creditCardNumber;
	private String retailerName;
	private float amount;
	private String street;
	private String city;
	private int zip;
	private String state;
	private String country;
	private float latitude;
	private float longitude;
	private int timestamp;
	
	public String toString(){
		String str = new String();
		str = str + " [ " + transactionId + ", "
				+ creditCardType + ", "
				+ creditCardNumber + ", "
				+ retailerName + ", "
				+ amount + ", "
				+ street + ", "
				+ city + ", "
				+ zip + ", "
				+ state + ", "
				+ country + ", "
				+ latitude + ", "
				+ longitude + ", "				
				+ timestamp
				+ " ] ";
		return str;
	}
	
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getCreditCardType() {
		return creditCardType;
	}

	public void setCreditCardType(String creditCardType) {
		this.creditCardType = creditCardType;
	}

	public int getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(int creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public String getRetailerName() {
		return retailerName;
	}

	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getZip() {
		return zip;
	}

	public void setZip(int zip) {
		this.zip = zip;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	
	   public byte[] toBytes() {
	        byte[]bytes;
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        try{
	            ObjectOutputStream oos = new ObjectOutputStream(baos);
	            oos.writeObject(this);
	            oos.flush();
	            oos.reset();
	            bytes = baos.toByteArray();
	            oos.close();
	            baos.close();
	        } catch(IOException e){
	            throw new RuntimeException(e);
	        }
	        return bytes;
	    }

	    public static Order fromBytes(byte[] body) {
	        Order obj = null;
	        try {
	            ByteArrayInputStream bis = new ByteArrayInputStream(body);
	            ObjectInputStream ois = new ObjectInputStream(bis); // Error here
	            obj = (Order) ois.readObject();
	            ois.close();
	            bis.close();
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }
	        catch (ClassNotFoundException ex) {
	            ex.printStackTrace();
	        }
	        return obj;
	    }	
	
}
