package com.cands.delini;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.cands.delini.asynctask.MyAsyncTask;
import com.cands.delini.listener.AsyncTaskCompleteListener;
import com.cands.delini.utility.Utilities;


public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtName, edtMobileNo, edtVehicleNo, edtPassword, edtCnfmPassword, edtEmail;

    private Button btnSubmit;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Utilities.getLoginUserName(this) != null){
            navigateToMain();
        }
        setContentView(R.layout.activity_registration);

        context = this;

        edtName = (EditText) findViewById(R.id.reg_name);
        edtMobileNo = (EditText) findViewById(R.id.reg_mobileNo);
        edtVehicleNo = (EditText) findViewById(R.id.reg_vehicleNo);
        edtEmail = (EditText) findViewById(R.id.reg_emailId);
        edtPassword = (EditText) findViewById(R.id.reg_password);
        edtCnfmPassword = (EditText) findViewById(R.id.reg_cnfmPassword);

        btnSubmit = (Button) findViewById(R.id.reg_submit);

        btnSubmit.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        /*if (Utilities.networkAvailability(context)) {
//                        showProgress();
            registrationProcess();
            //loginProcess();
        } else {
            Utilities.showAlert(context, null, getString(R.string.check_connection));
        }*/
        Utilities.setUserOInfo(context, getText(edtName), getText(edtMobileNo), getText(edtVehicleNo), getText(edtEmail), getText(edtPassword));
        navigateToMain();
    }

    private void navigateToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void registrationProcess() {
        String url = "";
        MyAsyncTask loginAsync = new MyAsyncTask("", completeListener, 30*1000, 30*1000);
        Utilities.showProgressDialog(context);
        loginAsync.execute(url);

    }

    private AsyncTaskCompleteListener completeListener = new AsyncTaskCompleteListener() {
        @Override
        public void onAsynComplete(String result) {
            if (result != null) {
                Utilities.showAlert(context, "", "Thank you registering with us");
                Utilities.setUserOInfo(context, getText(edtName), getText(edtMobileNo), getText(edtVehicleNo), getText(edtEmail), getText(edtPassword));
                navigateToMain();
            } else {
                Utilities.showAlert(context, "Network problem", "Check your internet connection");
            }
            Utilities.hideProgressDialog();
        }
    };

    private String getText(EditText edt){
        return edt.getText().toString().trim();
    }
}
