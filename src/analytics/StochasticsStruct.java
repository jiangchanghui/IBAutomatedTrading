package analytics;

public class StochasticsStruct {
//private double k = 0.0;
private double SlowSto = 0.0; //Slow Sto line, average of A and B
private double A = 0.0; //Close - lowest low
private double B = 0.0;//Highest high - lowest low
private double SignalLine = 0.0; //Signal Line (moving average of slow sto)


public StochasticsStruct(double SignalLine, double SlowSto, double A, double B) {
	//this.k = k;
	this.SlowSto = SlowSto;
	this.A = A;
	this.B = B;
	this.SignalLine = SignalLine;
	}



public double getA() {
	return A;
	}



public double getB() {
	return B;
}



public double getS() {
	return SlowSto;
}



}
