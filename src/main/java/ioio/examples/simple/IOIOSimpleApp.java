// use gnd (black) and 5v (red) use 40 and 41 for yellow sensors and 12 for yellow motor


package ioio.examples.simple;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class IOIOSimpleApp extends IOIOActivity {
	private TextView textView_;
	private TextView textView2_;
	private ToggleButton toggleButton_;
	private EditText sens1inp;
	private EditText sens2inp;

	int sens1wt = 1;
	int sens2wt = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView_ = (TextView) findViewById(R.id.TextView);
		textView2_ = (TextView) findViewById(R.id.TextView2);
		toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);
		sens1inp = (EditText) findViewById(R.id.sensor1input);
		sens2inp = (EditText) findViewById(R.id.sensor2input);

		enableUi(false);
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput input1_;
		private AnalogInput input2_;
		private PwmOutput pwmOutput_;
		private DigitalOutput led_;

		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			input1_ = ioio_.openAnalogInput(40);
			input2_ = ioio_.openAnalogInput(41);
			pwmOutput_ = ioio_.openPwmOutput(12, 100);
			enableUi(true);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
//			this is where the sensor is read. change this to "setNumber1(input1_.read());"
//			add line "setNumber2(input2_.read());"

			setNumber1(input1_.read());
			setNumber2(input2_.read());

			float sens1num = input1_.read();

			if (sens1num < .1) {
				sens1num = 0;
			}

			float sens2num = input2_.read();

			if (sens2num < .1) {
				sens2num = 0;
			}

			float result = 0;

			if (sens1wt != 0) {
				result = (sens1wt * sens1num + sens2wt * sens2num) / (sens1wt + sens2wt);;
			}
			else {
				if (sens2wt != 0) {
					result = (sens1wt * sens1num + sens2wt * sens2num) / (sens1wt + sens2wt);
				}
			}

//			this is where the power of the motor is calculated- don't use seekBar_.getProgress
//			use .getProgress of sensor
			pwmOutput_.setPulseWidth(500 + (int)(result * 2000));
			led_.write(!toggleButton_.isChecked());
			Thread.sleep(10);
		}

		@Override
		public void disconnected() {
			enableUi(false);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				toggleButton_.setEnabled(enable);
			}
		});
	}


	private void setNumber1(float f) {
		final String str = String.format("%.2f", f);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(str);
			}
		});
	}

	private void setNumber2(float f) {
		final String str = String.format("%.2f", f);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView2_.setText(str);
			}
		});
	}

	public void sendWeights (View view) {
		String sens1wtstr = sens1inp.getText().toString();
		String sens2wtstr = sens2inp.getText().toString();

		sens1wt = Integer.parseInt(sens1wtstr);
		sens2wt = Integer.parseInt(sens2wtstr);

	}
}