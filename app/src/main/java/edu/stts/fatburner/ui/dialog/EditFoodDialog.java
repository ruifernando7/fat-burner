package edu.stts.fatburner.ui.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import edu.stts.fatburner.R;
import edu.stts.fatburner.data.model.LogFood;
import edu.stts.fatburner.data.network.API;
import edu.stts.fatburner.data.network.ApiClient;
import edu.stts.fatburner.data.network.body.UpdateLogFoodBody;
import edu.stts.fatburner.data.network.response.InsertResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditFoodDialog extends DialogFragment implements View.OnClickListener {
    private LogFood logFood;
    public Callback callback;
    private TextView tvFoodName,tvSatuan,tvKalori;
    private EditText etJumlah;
    private Button btnUpdate,btnDelete;
    private ImageButton btnClose;
    private API mApiInterface;
    private SharedPreferences prefs;

    public static EditFoodDialog newInstance(LogFood data){
        EditFoodDialog instance = new EditFoodDialog();
        Bundle b = new Bundle();
        b.putSerializable("food_data",data);
        instance.setArguments(b);
        return instance;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_edit_food, container, false);
        tvFoodName = v.findViewById(R.id.dialog_editfood_foodname);
        tvKalori = v.findViewById(R.id.dialog_editfood_kalori);
        tvSatuan = v.findViewById(R.id.dialog_editfood_satuan);
        etJumlah = v.findViewById(R.id.dialog_editfood_jumlah);
        btnUpdate = v.findViewById(R.id.dialog_editfood_update);
        btnClose = v.findViewById(R.id.dialog_editfood_close);
        btnDelete = v.findViewById(R.id.dialog_editfood_delete);
        btnUpdate.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        mApiInterface = ApiClient.getClient().create(API.class);
        //listener buat edit text jika ada pergantian input
        etJumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")) {
                    Integer jumlah = Integer.parseInt(s.toString());
                    tvKalori.setText(String.valueOf(jumlah * Integer.parseInt(logFood.getKalori()+"")));
                }else tvKalori.setText("0");
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = getActivity().getSharedPreferences("FatBurnerPrefs",Context.MODE_PRIVATE);
        logFood = (LogFood) getArguments().getSerializable("food_data");
        etJumlah.setText(logFood.getJumlah()+"");
        etJumlah.setSelection(etJumlah.getText().length());
        tvFoodName.setText(logFood.getNama()+"");
        tvSatuan.setText(logFood.getSatuan()+"");
        tvKalori.setText(logFood.getKalori() * logFood.getJumlah() +"");
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.dialog_editworkout_close){
            Toast.makeText(requireContext(),"dismiss",Toast.LENGTH_LONG).show();
            dismiss();
        }else if(id == R.id.dialog_editworkout_update){
            if(!etJumlah.getText().toString().equals("") && !etJumlah.getText().toString().equals("0")){
                Toast.makeText(requireContext(),"msk",Toast.LENGTH_LONG).show();
                updateLogFood(Integer.parseInt(etJumlah.getText().toString()));
            }else Toast.makeText(requireContext(),"Field size must not be empty!",Toast.LENGTH_LONG).show();
        }else if(id == R.id.dialog_editworkout_delete){
            deleteLogFood();
        }
    }

    private void updateLogFood(int jumlah){
        UpdateLogFoodBody body = new UpdateLogFoodBody(jumlah);
        String token = prefs.getString("token","");
        Call<InsertResponse> saveCall = mApiInterface.updateLogFood(token,logFood.getId_log(),body);
        saveCall.enqueue(new retrofit2.Callback<InsertResponse>() {
            @Override
            public void onResponse(Call<InsertResponse> call, Response<InsertResponse> res) {
                InsertResponse response = res.body();
                Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();
                if(!response.isError()){
                    callback.perform(true);
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<InsertResponse> call, Throwable t) {
                Toast.makeText(requireContext(),t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteLogFood(){
        String token = prefs.getString("token","");
        Call<InsertResponse> deleteCall = mApiInterface.deleteLogFood(token,logFood.getId_log());
        deleteCall.enqueue(new retrofit2.Callback<InsertResponse>() {
            @Override
            public void onResponse(Call<InsertResponse> call, Response<InsertResponse> res) {
                InsertResponse response = res.body();
                Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();
                if(!response.isError()){
                    callback.perform(true);
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<InsertResponse> call, Throwable t) {
                Toast.makeText(requireContext(),t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface Callback {
        void perform(boolean success);
    }
}
