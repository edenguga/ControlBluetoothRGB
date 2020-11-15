package com.example.control_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothSocket btSocket;
    String ConexionBT = "NO CONECTADO";
    private static final int REQUEST_CODE = 1269;
    String DIR_MAC_BT = "";
    ProgressBar pBar;
    Button btnConectBT;
    ConectarBT conectarBT;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnRojo = findViewById(R.id.btn_Rojo);
        Button btnVerde = findViewById(R.id.btn_Verde);
        Button btnAzul = findViewById(R.id.btn_Azul);
        Spinner spinnerRojo = findViewById(R.id.spinnerRojo);
        Spinner spinnerVerde = findViewById(R.id.spinnerVerde);
        Spinner spinnerAzul = findViewById(R.id.spinnerAzul);
        btnConectBT = findViewById(R.id.btn_ConectarBT);
        pBar = findViewById(R.id.progressBar);

        //Instanciar la Tarea Asincrona (AsyncTask) para conectar con el Bluetooth
        conectarBT = new ConectarBT();

        //adaptador de Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Lanzar el Activity control_bt con Result para que nos devuelva la MAC del Bluetooth
        btnConectBT.setOnClickListener(view -> {
            if (ConexionBT.equals("NO CONECTADO")) {
              Intent intent = new Intent(view.getContext(), ControlBT.class);
              startActivityForResult(intent, REQUEST_CODE);
          }
            else if (ConexionBT.equals("CONECTADO")){
                showToast("Apagando El Bluetooth");
                try {
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBluetoothAdapter.disable();
                btnConectBT.setText(R.string.btnConBT3);
                ConexionBT = null;
            }else{
                System.exit(0);
            }
        });

        //Botón Rojo para encender y apagar el color rojo del LED RGB
        btnRojo.setOnClickListener(new View.OnClickListener() {
            int ctrlEncendido = 0;
            @Override
            public void onClick(View view) {
                if (ctrlEncendido == 0) {
                    EnviarDatos("B255");
                    ctrlEncendido = 1;

                }
                else if (ctrlEncendido == 1) {
                    EnviarDatos("B0");
                    ctrlEncendido = 0;
                }
            }
        });

        //Botón Verde para encender y apagar el color verde del LED RGB
        btnVerde.setOnClickListener(new View.OnClickListener() {
            int ctrlEncendido = 0;
            @Override
            public void onClick(View view) {
                if (ctrlEncendido == 0) {
                    EnviarDatos("C255");
                    ctrlEncendido = 1;

                }
                else if (ctrlEncendido == 1) {
                    EnviarDatos("C0");
                    ctrlEncendido = 0;
                }
            }
        });

        //Botón Azul para encender y apagar el color azul del LED RGB
        btnAzul.setOnClickListener(new View.OnClickListener() {
            int ctrlEncendido = 0;
            @Override
            public void onClick(View view) {
                if (ctrlEncendido == 0) {
                    EnviarDatos("A255");
                    ctrlEncendido = 1;

                }
                else if (ctrlEncendido == 1) {
                    EnviarDatos("A0");
                    ctrlEncendido = 0;
                }
            }
        });

        spinnerRojo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String datos;
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                    datos = ("B" + spinnerRojo.getItemAtPosition(position));
                    EnviarDatos(datos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {

            }
        });

        spinnerVerde.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String datos;
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                datos = ("C" + spinnerVerde.getItemAtPosition(position));
                EnviarDatos(datos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {

            }
        });

        spinnerAzul.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String datos;
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                datos = ("A" + spinnerAzul.getItemAtPosition(position));
                EnviarDatos(datos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {

            }
        });

    }

    //Evaluar el resultado y proceder de acuerdo con lo recibido
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                DIR_MAC_BT = data.getStringExtra("MESSAGE_MAC");

                //Las dos lineas siguientes permiten convertir la MAC que está en String a Bluetooth Device
                BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothDevice mBluetoothDevice = bluetoothManager.getAdapter() .getRemoteDevice(DIR_MAC_BT);
                conectarBT.execute(mBluetoothDevice);

            } else if(resultCode == RESULT_CANCELED){
                showToast("Operación Cancelada por el usuario");
            }
        }
    }

    //Método para enviar mensajes toast (Mensajes Emergentes)
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //Ejecución de Tarea Asincrona para conectar con el Bluetooth
    private class ConectarBT extends AsyncTask <BluetoothDevice, Integer, BluetoothSocket> {

        private BluetoothSocket mmSocket;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... device) {
            BluetoothDevice mmDevice = device[0];
            try {
                String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";
                mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(mmUUID));
                mmSocket.connect();
                ConexionBT = "CONECTADO";

            } catch (Exception e) {
                ConexionBT = "NO CONECTADO";
            }

            return mmSocket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket result) {
            pBar.setVisibility(View.INVISIBLE);
            if(ConexionBT.equals("NO CONECTADO")){
                showToast("Error en la conexión, dispositivo remoto no disponible...");
                btnConectBT.setText(R.string.btnConBT3);
                mBluetoothAdapter.disable();
                ConexionBT = "";
            }else {
                showToast("Dispositivo conectado...");
                ConexionBT = "CONECTADO";
                btnConectBT.setText(R.string.btnConBT2);
            }
            btSocket = result;
        }
    }

    //Método para enviar datos por Bluetooth en este caso a un Arduino o STM y controlar luces LED RGB
        public void EnviarDatos (String dato) {

            OutputStream mmOutStream;

            if (ConexionBT.equals("CONECTADO")) try {
                if (btSocket.isConnected()) {
                    mmOutStream = btSocket.getOutputStream();
                    mmOutStream.write(dato.getBytes());
                }

            } catch (IOException e) {
                Log.v("EEGG", "Error no hay conexión");
            }
        }
}


//
// EEGG: Log.v("EEGG", "Aquí se coloca el mensaje o la variable o ambos");

/*
    //Crea una tarea asíncrona en segundo plano (AsynTask) sin parámetros para conectarse al dispositivo Bluetooth
    //Si no se hiciera en segundo plano, al estar bloqueado el hilo principal, si se tocara en pantalla apareceria error
    private class ConectarBT extends AsyncTask <Void,Void,Void>
    {
        //Crea una variable para indicar en la tarea que se ha conectado al dispositivo BT
        private boolean Sehaconectado=true; //Inicialmente suponemos que nos conectaremos correctamente
         @Override
        protected void onPreExecute() {
        super.onPreExecute();
    //Muestra una rueda de progreso con el título Conectando y el texto Paciencia
            progresoind.setVisibility(View.VISIBLE);
        }

        //Mientras muestra la rueda, intenta conectar al dispositivo
        @Override
        protected Void doInBackground(Void... params) {
    //Si no existe una conexión anterior o no esta conectado el dispositivo, intenta conectarse con control de error
            try
            {
                if (btSocket==null||!BTconectado)
                {
    //Asigna el adaptador bluetooth del propio dispositivo
                  BTAdapter=BluetoothAdapter.getDefaultAdapter();
    //Crea un objeto disposito y lo asigna al BT remoto mediante la dirección
                  BluetoothDevice dispositivo=BTAdapter.getRemoteDevice(DIR_MAC_BT);
    //Asigna al objeto Socket, una conexión serie RFComm con eel dispositivo remoto
                    btSocket=dispositivo.createInsecureRfcommSocketToServiceRecord(UUIDserie);
    //Deja de buscar dispositivos remotos
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    //Recordar poner en el AndroidManifest : <usespermission android:name="android.permission.BLUETOOTH_ADMIN" />
    //Intenta iniciar la conexión con el dispositivo remoto
                    btSocket.connect();
                }
            }
    //En caso de error de conexión indica que no se ha conectado
            catch (IOException e)
            {
                Sehaconectado=false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
    //Verificar si funciona sin la siguiente linea
    //super.onPostExecute(aVoid);
    //Tras intentar conectarse, si no se conecta, indica el error y finaliza la tarea ConectarBT
            if (!Sehaconectado)
            {
                String msg="Error de conexión. El dispositivo remoto NO soporta Bluetooth serie SPP? Prueba otra vez";
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                finish(); //Finaliza la actividad actual y la cierra
            }
            else //Si se ha conectado indica que se ha conectado
            {
                Toast.makeText(getApplicationContext(),"Conectado",Toast.LENGTH_SHORT).show();
                BTconectado=true;
    //Activa el Thread en segundo plano para la lectura de bluetooth HiloLectura = new HiloConexion();
                HiloLectura.start();
            }
            progresoind.dismiss(); //Finaliza el indicador de la rueda de progreso
        }
    }

 */