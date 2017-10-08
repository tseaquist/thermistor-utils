package steinhart;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import temp.Celsius;
import temp.TempSystem;

public class Steinhart {
	
	public TempSystem tempSystem;
	public final double A0;
	public final double A1;
	public final double A2;
	public final double A3;
	
	public Steinhart(TempSystem tempSystem, double A0, double A1, double A3)
	{
		this(tempSystem, A0, A1, 0, A3);
	}
	
	public Steinhart(TempSystem tempSystem, double A0, double A1, double A2, double A3)
	{
		this.tempSystem = tempSystem;
		this.A0 = A0;
		this.A1 = A1;
		this.A2 = A2;
		this.A3 = A3;
	}
	
	public void changeUnits(TempSystem tempSystem)
	{
		this.tempSystem = tempSystem;
	}

	public double getTemp(double resistance)
	{
		  double lnResitance = Math.log(resistance); 
		  //Build up the number in the stages.
		  double temp = A3;
		  temp = temp * lnResitance + A2;
		  temp = temp * lnResitance + A1;
		  temp = temp * lnResitance + A0;
		  return tempSystem.fromKelvin(temp);
	}
	
	public static Steinhart calibrate(TempSystem tempSystem,
			double resistance1, double temp1,
			double resistance2, double temp2,
			double resistance3, double temp3,
			double resistance4, double temp4)
	{
		RealMatrix resistanceMatrix = formCalibrationMatrix(resistance1, resistance2, resistance3, resistance4);
		RealVector tempVector = new ArrayRealVector(new ArrayRealVector(new double[] {
				1/tempSystem.toKelvin(temp1), 
				1/tempSystem.toKelvin(temp2), 
				1/tempSystem.toKelvin(temp3), 
				1/tempSystem.toKelvin(temp4)}));
		DecompositionSolver solver = new LUDecomposition(resistanceMatrix).getSolver();
		RealVector steinhartCoefs = solver.solve(tempVector);
		return new Steinhart(tempSystem, 
				steinhartCoefs.getEntry(0), 
				steinhartCoefs.getEntry(1),
				steinhartCoefs.getEntry(2),
				steinhartCoefs.getEntry(3));
	}
	
	public static Steinhart calibrate(TempSystem tempSystem,
			double resistance1, double temp1,
			double resistance2, double temp2,
			double resistance3, double temp3)
	{
		RealMatrix resistanceMatrix = formCalibrationMatrix(resistance1, resistance2, resistance3);
		RealVector tempVector = new ArrayRealVector(new ArrayRealVector(new double[] {
				1/tempSystem.toKelvin(temp1), 
				1/tempSystem.toKelvin(temp2), 
				1/tempSystem.toKelvin(temp3)}));
		DecompositionSolver solver = new LUDecomposition(resistanceMatrix).getSolver();
		RealVector steinhartCoefs = solver.solve(tempVector);
		
		return new Steinhart(tempSystem, 
				steinhartCoefs.getEntry(0), 
				steinhartCoefs.getEntry(1),
				steinhartCoefs.getEntry(2));
	}
	
	protected static RealMatrix formCalibrationMatrix(double... resistance)
	{
		int n = resistance.length;
		if(!(n == 3 || n == 4))
		{
			throw new RuntimeException("Can only except 3 or 4 arguments.");
		}
		double[][] matrixData = new double[n][n];
		for(int rowNum = 0; rowNum < n; rowNum++)
		{
			double[] row = matrixData[rowNum];
			double lnResistance = Math.log(resistance[rowNum]);
			double lnPow = 1;
			for(int colNum = 0; colNum < n; colNum++)
			{
				if(n==3 && colNum ==2)
				{
					lnPow *= lnResistance;
				}
				row[colNum] = lnPow;
				lnPow *= lnResistance;
			}
		}
		return new Array2DRowRealMatrix(matrixData);
	}
	
	public static Steinhart fromBeta( TempSystem tempSystem, double beta, double refTemp, double refResistance)
	{
		double A0 = 1 / tempSystem.toKelvin(refTemp) - Math.log(refResistance) / beta;
		double A1 = 1 / beta;
		return new Steinhart(tempSystem, A0, A1, 0);
	}
	
	@Override
	public String toString()
	{
		return "A0: " + A0 + 
				"\nA1: " + A1 +
				"\nA2: " + A2 +
				"\nA3: " + A3;
	}
	
	public static void main(String[] args) {
		Steinhart st = calibrate( new Celsius(), 
				10_000, 25, 
				2953.8792231522, 55, 
				1070.3092720327, 85);
		Steinhart stb = fromBeta(new Celsius(), 3950, 25, 10_000);
		System.out.println(st);
		System.out.println(stb);
	}
}
