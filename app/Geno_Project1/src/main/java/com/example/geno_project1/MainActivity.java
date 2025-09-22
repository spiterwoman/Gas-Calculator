package com.example.geno_project1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText editDistance, editCPG, editHighwayMpg;
    private CheckBox cbAggressive;
    private RadioGroup rgNeedAc;
    private Spinner roadTypeSpinner;
    private SeekBar speedSeek;
    private TextView speedNumber, showResult;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editDistance    = findViewById(R.id.editDistance);
        editCPG         = findViewById(R.id.editCPG);
        editHighwayMpg  = findViewById(R.id.editHighwayMpg);
        cbAggressive    = findViewById(R.id.cbAggressive);
        rgNeedAc        = findViewById(R.id.rgNeedAc);
        roadTypeSpinner = findViewById(R.id.roadTypeSpinner);
        speedSeek       = findViewById(R.id.speedSeek);
        speedNumber     = findViewById(R.id.tvSpeedNumber);
        showResult      = findViewById(R.id.showResult);
        btnCalculate    = findViewById(R.id.btnCalculate);

        // initial UI for speed
        speedNumber.setText(String.valueOf(progressToMph(speedSeek.getProgress())));
        speedSeek.setTooltipText(String.valueOf(progressToMph(speedSeek.getProgress())));
        speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int mph = progressToMph(progress);
                speedNumber.setText(String.valueOf(mph));
                seekBar.setTooltipText(String.valueOf(mph));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnCalculate.setOnClickListener(v -> calculateAndDisplay());
    }

    private int progressToMph(int progress) { return 35 + progress * 5; }

    private void calculateAndDisplay() {
        Double distance = readDouble(editDistance, "Enter distance");
        Double costPerGallon = readDouble(editCPG, "Enter cost per gallon");
        Double highwayMpg = readDouble(editHighwayMpg, "Enter highway MPG");
        if (distance == null || costPerGallon == null || highwayMpg == null) return;

        int acChoiceId = rgNeedAc.getCheckedRadioButtonId();
        if (acChoiceId == View.NO_ID) {
            Toast.makeText(this, "Please select Need A/C (Yes or No).", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean aggressive = cbAggressive.isChecked();
        boolean acOn = acChoiceId == R.id.rgNeedAcYes;
        String roadType = (roadTypeSpinner.getSelectedItem() != null)
                ? roadTypeSpinner.getSelectedItem().toString()
                : "Highway";
        int speed = progressToMph(speedSeek.getProgress());

        int modifier = 0;
        if (acOn) modifier += 15;
        if (speed > 50) {
            int over = speed - 50;
            modifier += (over / 5) * 5;
        }

        if (aggressive) {
            switch (roadType) {
                case "Highway": modifier += 15; break;
                case "City":    modifier += 25; break;
                case "Mixed":   modifier += 20; break;
            }
        } else {
            switch (roadType) {
                case "Highway": modifier += 0;  break;
                case "City":    modifier += 15; break;
                case "Mixed":   modifier += 10; break;
            }
        }

        double finalMpg = highwayMpg * ((100 - modifier) / 100.0);
        if (finalMpg <= 0) {
            showResult.setText("Final MPG is <= 0. Enter a number > 0");
            return;
        }

        double gallons = (2.0 * distance) / finalMpg;
        double totalCost = gallons * costPerGallon;
        String amount = NumberFormat.getCurrencyInstance(Locale.US).format(totalCost);
        showResult.setText("You'll need to spend this much on a round trip:\n" + amount);
    }

    private Double readDouble(EditText et, String errorMsg) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) {
            et.setError(errorMsg);
            et.requestFocus();
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            et.setError("Invalid number");
            et.requestFocus();
            return null;
        }
    }
}