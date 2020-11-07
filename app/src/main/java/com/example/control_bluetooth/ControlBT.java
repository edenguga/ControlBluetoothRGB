package com.example.control_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ControlBT extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    private ArrayAdapter<String> mArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket btSocket;
    private ArrayList<BluetoothDevice> btDeviceArray = new ArrayList<BluetoothDevice>();
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String MAC_BT = "", NOM_BT = "";

    TextView mtV_EstadoBT;
    ListView mLV_EmparejadosBT;
    ImageView mImg_Est_BT;
    Button mbtn_EncenderBT, mbtn_ApagarBT, mbtn_Regresar, mbtn_EmparejadosBT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_bt);

        mtV_EstadoBT = findViewById(R.id.tV_EstadoBluetooth);
        mLV_EmparejadosBT = findViewById(R.id.LV_EmparejadosBT);
        mImg_Est_BT = findViewById(R.id.iV_EstadoBluetooth);
        mbtn_EncenderBT = findViewById(R.id.btn_EncenderBT);
        mbtn_ApagarBT = findViewById(R.id.btn_ApagarBT);
        mbtn_Regresar = findViewById(R.id.btn_Regresar);
        mbtn_EmparejadosBT = findViewById(R.id.btn_EmparejadosBT);

        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mLV_EmparejadosBT.setAdapter(mArrayAdapter);

        //adaptador de Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Revisar si El Bluetooth está disponible en el teléfono
        if (mBluetoothAdapter == null) {
            mtV_EstadoBT.setText("No disponible");
        } else {
            mtV_EstadoBT.setText("Disponible");
        }

        //Colocar imagen de acuerdo con el estado del Bluetooth (on/off)
        if (mBluetoothAdapter.isEnabled()) {
            mImg_Est_BT.setImageResource(R.drawable.bluetooth_on);
        } else {
            mImg_Est_BT.setImageResource(R.drawable.bluetooth_off);
        }

        //Botón para Encender el Bluetooth
        mbtn_EncenderBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    showToast("Encendiendo El Bluetooth");
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    showToast("Bluetooth Encendido");
                }
            }
        });

        //Botón para Apagar el Bluetooth
        mbtn_ApagarBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    showToast("Apagando El Bluetooth");
                    mImg_Est_BT.setImageResource(R.drawable.bluetooth_off);
                } else {
                    showToast("El Bluetooth está apagado");
                }
            }
        });

        //Botón para mostrar los dispositivos emparejados
        mbtn_EmparejadosBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        //Botón para regresar al main activity enviando la MAC del dispositivo seleccionado
        //Si se utiliza el boton atrás no se envía la MAC
        mbtn_Regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cambia_a_Main();
            }
        });

        //Cuando se selecciona un dispositivo emparejado:
        mLV_EmparejadosBT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = btDeviceArray.get(position);
                MAC_BT = device.getAddress().toString();
                NOM_BT = device.getName().toString();
            }
        });

    }

    //Evaluar el resultado del encendido del Bluetooth y cambiar la imagen off / on
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //El Bluetooth es encendido
                    mImg_Est_BT.setImageResource(R.drawable.bluetooth_on);
                    showToast("Bluetooth Encendido");
                } else {
                    //El usuario denegó el encendido del Bluetooth
                    showToast("No se pudo Encender El Bluetooth");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Método para enviar mensajes toast (Mensajes Emergentes)
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //Método para cambiar de Activity
    public void Cambia_a_Main() {
        Intent intent = new Intent();
        intent.putExtra("MESSAGE_MAC", MAC_BT);
        intent.putExtra("MESSAGE_NOM", NOM_BT);
        setResult(RESULT_OK, intent);
        finish();
    }
}

/*    private class ConnectAsyncTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket> {

        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... device) {
            mmDevice = device[0];
            try {
                String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";
                mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(mmUUID));
                mmSocket.connect();
            } catch (Exception e) {
            }
            return mmSocket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket result) {
            btSocket = result;
        }
    }
*/

/*
        // Instance AsyncTask
        connectAsyncTask = new ConnectAsyncTask();
*/