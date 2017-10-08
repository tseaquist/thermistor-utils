package temp;

public class Celsius implements TempSystem
{

	@Override
	public double fromKelvin(double kelvin) {
		return TempConversion.kelvinToCelsius(kelvin);
	}

	@Override
	public double toKelvin(double celsius) {
		return TempConversion.celsiusToKelvin(celsius);
	}

}
