package com.example.geno_project1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText editDistance, editCPG, editHighwayMpg;
    private CheckBox cbAggressive;
    private RadioGroup rgNeedAc;
    private Spinner roadTypeSpinner;
    private Slider sliderSpeed;

    private TextView tvSpeedValue, showResult;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editDistance   = findViewById(R.id.editDistance);
        editCPG        = findViewById(R.id.editCPG);
        editHighwayMpg = findViewById(R.id.editHighwayMpg);
        cbAggressive   = findViewById(R.id.cbAggressive);
        rgNeedAc       = findViewById(R.id.rgNeedAc);
        roadTypeSpinner= findViewById(R.id.roadTypeSpinner);
        sliderSpeed    = findViewById(R.id.sliderSpeed);
        tvSpeedValue   = findViewById(R.id.tvSpeedValue);
        showResult     = findViewById(R.id.showResult);
        btnCalculate   = findViewById(R.id.btnCalculate);


        // Slider label + live value text
        sliderSpeed.setLabelFormatter(value -> Math.round(value) + " mph");
        tvSpeedValue.setText(Math.round(sliderSpeed.getValue()) + " mph");
        sliderSpeed.addOnChangeListener((s, value, fromUser) ->
                tvSpeedValue.setText(Math.round(value) + " mph"));
        btnCalculate.setOnClickListener(v -> calculateAndDisplay());
    }

    private void calculateAndDisplay() {
        Double distance = readDouble(editDistance, "Enter distance");
        Double costPerGallon = readDouble(editCPG, "Enter cost per gallon");
        Double highwayMpg = readDouble(editHighwayMpg, "Enter highway MPG");
        if (distance == null || costPerGallon == null || highwayMpg == null) return;

        boolean aggressive = cbAggressive.isChecked();
        boolean acOn = rgNeedAc.getCheckedRadioButtonId() == R.id.rgNeedAcYes;
        String roadType = (roadTypeSpinner.getSelectedItem() != null)
                ? roadTypeSpinner.getSelectedItem().toString()
                : "Highway"; // fallback
        int speed = Math.round(sliderSpeed.getValue());

        int modifier = 0;
        if (acOn) modifier += 15;
        if (speed > 50) {
            int over = speed - 50;
            modifier += (over / 5) * 5;
        }

        if (aggressive) {
            if ("Highway".equals(roadType)) modifier += 15;
            else if ("City".equals(roadType)) modifier += 25;
            else if ("Mixed".equals(roadType)) modifier += 20;
        } else {
            if ("Highway".equals(roadType)) modifier += 0;
            else if ("City".equals(roadType)) modifier += 15;
            else if ("Mixed".equals(roadType)) modifier += 10;
        }

        double finalMpg = highwayMpg * ((100 - modifier) / 100.0);
        if (finalMpg <= 0) {
            showResult.setText("Final MPG is <= 0. Check inputs.");
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
