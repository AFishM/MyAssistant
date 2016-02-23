package com.name.myassistant;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TimePicker;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimeFragment} factory method to
 * create an instance of this fragment.
 */
public class TimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    public TimeFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

    }
}
