package dev.atharvakulkarni.e_commerce.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.payoneer.checkout.model.PresetAccount;
import com.payoneer.checkout.ui.PaymentActivityResult;
import com.payoneer.checkout.ui.PaymentResult;
import com.payoneer.checkout.ui.PaymentTheme;
import com.payoneer.checkout.ui.PaymentUI;
import com.payoneer.checkout.ui.page.PaymentListActivity;

import java.util.Map;

import dev.atharvakulkarni.e_commerce.R;
import dev.atharvakulkarni.e_commerce.databinding.OrderPlacingBinding;
import dev.atharvakulkarni.e_commerce.net.PayoneerApiClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class order_placing extends AppCompatActivity
{
    private final static int PAYMENT_REQUEST_CODE = 1;
    private PaymentActivityResult activityResult;
    OrderPlacingBinding binding;
    SeekBar seekBar;
    Button deliver_here,continue_button, continue_payment;
    LinearLayout address,order_summary,linearLayout2;
    ConstraintLayout payment;
    private SwitchMaterial themeSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.order_placing);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white,getTheme()));


        seekBar = binding.seekbar;
        deliver_here = binding.deliverHere;
        continue_button = binding.continueButton;
        address  = binding.address;
        order_summary = binding.orderSummary;
        linearLayout2 = binding.linearlayout2;
        payment = binding.payment;
        continue_payment = binding.continuePayment;

        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green2,getTheme()), PorterDuff.Mode.SRC_ATOP);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.green2,getTheme()), PorterDuff.Mode.SRC_ATOP);

        seekBar.setProgress(10);

        deliver_here.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                address.setVisibility(View.GONE);
                order_summary.setVisibility(View.VISIBLE);

                seekBar.setProgress(48);
            }
        });

        continue_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                order_summary.setVisibility(View.GONE);
                payment.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.VISIBLE);

                seekBar.setProgress(88);
            }
        });

        continue_payment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                order_summary.setVisibility(View.GONE);
                payment.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.VISIBLE);

                seekBar.setProgress(100);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(order_placing.this);
                alertDialogBuilder.setTitle("List Request to Payoneer");

                /*Create handle for the RetrofitInstance interface*/
                PayoneerApiClient client = PayoneerApiClient.getInstance();
                Call<Map<String, Object>> call = client.getApi().getListUrl();
                call.enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                        String json = new Gson().toJson(response.body());
                        JsonParser parser = new JsonParser();
                        JsonObject obj = parser.parse(json).getAsJsonObject();
                        JsonObject links = obj.getAsJsonObject("links");
                        String listUrl = links.get("self").getAsString();
                        alertDialogBuilder.setCancelable(true);
                        alertDialogBuilder.setTitle("Payoneer-Step 2: Create a Payment Session");
                        alertDialogBuilder.setMessage("listUrl:" + listUrl);
                        alertDialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Request code to identify the response in onActivityResult()
                                int PAYMENT_REQUEST_CODE = 1;

//                                PaymentTheme.Builder builder = PaymentTheme.createBuilder();
//                                builder.setPaymentListTheme(com.payoneer.checkout.R.style.ThemeOverlay_Payment_Toolbar);
//                                builder.setChargePaymentTheme(com.payoneer.checkout.R.style.Base_Theme_MaterialComponents_Light);

                                // Show the charge preset account page
                                PaymentUI paymentUI = PaymentUI.getInstance();
                                paymentUI.setListUrl(listUrl);
                                //paymentUI.setPaymentTheme(builder.build());
                                paymentUI.showPaymentPage(order_placing.this, PAYMENT_REQUEST_CODE);
                            }
                        });
                        alertDialogBuilder.show();
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        //progressDoalog.dismiss();
                        Toast.makeText(order_placing.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        seekBar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            activityResult = PaymentActivityResult.fromActivityResult(requestCode, resultCode, data);
            handlePaymentActivityResult(activityResult);
        }
    }

    private void handlePaymentActivityResult(PaymentActivityResult activityResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(order_placing.this);
        builder.setTitle("Payoneer-Step 4: Handle Payment Activity Result");

        switch (activityResult.getResultCode()) {
            case PaymentActivityResult.RESULT_CODE_PROCEED:
            case PaymentActivityResult.RESULT_CODE_ERROR:
                PaymentResult paymentResult = activityResult.getPaymentResult();
                builder.setCancelable(true);
                builder.setMessage("Result Info:\n" + activityResult.getPaymentResult().getResultInfo());
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Request code to identify the response in onActivityResult()
                    }
                });
                builder.show();
                break;

            case Activity.RESULT_CANCELED:
                builder.setCancelable(true);
                builder.setMessage("Result Info:\n" + activityResult.getPaymentResult().getResultInfo());
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Request code to identify the response in onActivityResult()
                    }
                });
                builder.show();
                break;
                // This resultCode is returned when the user closed the payment page
        }
    }
//        private PaymentTheme createPaymentTheme() {
//        if (themeSwitch.isChecked()) {
//            return PaymentTheme.createBuilder().
//                    setPaymentListTheme(R.style.CustomTheme_Toolbar).
//                    setChargePaymentTheme(R.style.CustomTheme_NoToolbar).
//                    build();
//        } else {
//            return PaymentTheme.createDefault();
//        }
//    }

        /*
        order_placing.sliderListener sldListener = new order_placing.sliderListener();
        seekBar.setOnSeekBarChangeListener(sldListener);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            int stepSize = 3;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b)
            {
                // progress = ((int)Math.round(progress/stepSize))*stepSize;
                seekBar.setProgress(progress);

                Toast.makeText(order_placing.this, progress+"", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });*/


    /*private class sliderListener implements SeekBar.OnSeekBarChangeListener
    {
        private int smoothnessFactor = 10;
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            progress = Math.round(progress / smoothnessFactor);
            TextView lblProgress = (TextView) findViewById(R.id.seekbar);
            lblProgress.setText(String.valueOf(progress));
        }

        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        public void onStopTrackingTouch(SeekBar seekBar)
        {
            seekBar.setProgress(Math.round((seekBar.getProgress() + (smoothnessFactor / 2)) / smoothnessFactor) * smoothnessFactor);
        }
    }*/
}