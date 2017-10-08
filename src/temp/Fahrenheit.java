package temp;

public class Fahrenheit implements TempSystem {

	@Override
	public double fromKelvin(double kelvin) {
		return TempConversion.kelvinToFahrenheit(kelvin);
	}

	@Override
	public double toKelvin(double fahrenheit) {
		return TempConversion.fahrenheitToKelvin(fahrenheit);
	}

}
