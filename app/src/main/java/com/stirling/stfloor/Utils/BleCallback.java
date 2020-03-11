package com.stirling.stfloor.Utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public class BleCallback {

    public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState){}

    public void onBleServiceDiscovered(BluetoothGatt gatt, int status){}

    public void onBleCharacteristicChange(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic){}

    public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                           int status){
        Log.i("BLE", "onBleWrite sucedió");
    }

    public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                          int status){
        Log.i("BLE", "onBleRead sucedió");
    }


}
