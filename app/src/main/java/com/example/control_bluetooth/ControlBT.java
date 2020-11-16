package com.example.control_bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class ControlBT extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 2212;
    private ArrayAdapter<String> mArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private final ArrayList<BluetoothDevice> btDeviceArray = new ArrayList<>();
    String MAC_BT = "", NOM_BT = "";
    TextView mtV_EstadoBT;
    ListView mLV_EmparejadosBT;
    ImageView mImg_Est_BT;
    Button mbtn_EncenderBT, mbtn_ApagarBT, mbtn_EmparejadosBT;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_bt);
        mtV_EstadoBT = findViewById(R.id.tV_EstadoBluetooth);
        mLV_EmparejadosBT = findViewById(R.id.LV_EmparejadosBT);
        mImg_Est_BT = findViewById(R.id.iV_EstadoBluetooth);
        mbtn_EncenderBT = findViewById(R.id.btn_EncenderBT);
        mbtn_ApagarBT = findViewById(R.id.btn_ApagarBT);
        mbtn_EmparejadosBT = findViewById(R.id.btn_EmparejadosBT);

        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mLV_EmparejadosBT.setAdapter(mArrayAdapter);

        //adaptador de Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Revisar si El Bluetooth está disponible en el teléfono
        if (mBluetoothAdapter == null) {
            mtV_EstadoBT.setText("Bluetooth No disponible");
        } else {
            mtV_EstadoBT.setText("Bluetooth Disponible");
        }

        //Colocar imagen de acuerdo con el estado del Bluetooth (on/off)
        if (mBluetoothAdapter.isEnabled()) {
            mImg_Est_BT.setImageResource(R.drawable.bluetooth_on);
            mbtn_ApagarBT.setEnabled(true);
            mbtn_EmparejadosBT.setEnabled(true);
        } else {
            mImg_Est_BT.setImageResource(R.drawable.bluetooth_off);
            mbtn_EncenderBT.setEnabled(true);
        }

        //Botón para Encender el Bluetooth
        mbtn_EncenderBT.setOnClickListener(v -> {
            if (!mBluetoothAdapter.isEnabled()) {
                showToast("Encendiendo El Bluetooth");
                //intent to on bluetooth
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            } else {
                showToast("Bluetooth Encendido");
            }
        });

        //Botón para Apagar el Bluetooth
        mbtn_ApagarBT.setOnClickListener(v -> {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                showToast("Apagando El Bluetooth");
                mImg_Est_BT.setImageResource(R.drawable.bluetooth_off);
                mbtn_ApagarBT.setEnabled(false);
                mbtn_EmparejadosBT.setEnabled(false);
                mbtn_EncenderBT.setEnabled(true);
            } else {
                showToast("El Bluetooth está apagado");
            }
        });

        //Botón para mostrar los dispositivos emparejados
        mbtn_EmparejadosBT.setOnClickListener(v -> {
            if (mBluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    btDeviceArray.add(device);
                }
            } else {
                //El Bluetooth se encuentra apagado no se puede ver los dispositivos emparejados
                showToast("Encienda El Bluetooth para ver Disposivos Emparejados");
            }
        });

        //Cuando se selecciona un dispositivo emparejado:
        mLV_EmparejadosBT.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = btDeviceArray.get(position);
            MAC_BT = device.getAddress();
            NOM_BT = device.getName();
            Intent intent = new Intent();
            intent.putExtra("MESSAGE_MAC", MAC_BT);
            intent.putExtra("MESSAGE_NOM", NOM_BT);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    //Evaluar el resultado del encendido del Bluetooth y cambiar la imagen off / on
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //El Bluetooth es encendido
                mImg_Est_BT.setImageResource(R.drawable.bluetooth_on);
                showToast("Bluetooth Encendido");
                mbtn_EncenderBT.setEnabled(false);
                mbtn_EmparejadosBT.setEnabled(true);
                mbtn_ApagarBT.setEnabled(true);
            } else {
                //El usuario denegó el encendido del Bluetooth
                showToast("No se pudo Encender El Bluetooth");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Método para enviar mensajes toast (Mensajes Emergentes)
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
