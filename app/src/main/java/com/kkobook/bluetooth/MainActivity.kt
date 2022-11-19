package com.kkobook.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kkobook.bluetooth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestBluetoothPermission()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            binding.bluetoothStatusTv.text = "Bluetooth is not available"
        } else {
            binding.bluetoothStatusTv.text = "Bluetooth is available"
        }

        if (bluetoothAdapter.isEnabled) {
            binding.bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
        } else {
            binding.bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
        }

        binding.turnOnBtn.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Already on", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
            }
        }

        binding.turnOffBtn.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Already off", Toast.LENGTH_LONG).show()
            } else {
                bluetoothAdapter.disable()
                binding.bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
                Toast.makeText(this, "Bluetooth is off", Toast.LENGTH_LONG).show()
            }
        }

        binding.discoverableBtn.setOnClickListener {
            if (!bluetoothAdapter.isDiscovering) {
                Toast.makeText(this, "Making your device discoverable", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
                startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
            }
        }

        binding.pairedDevicesBtn.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                binding.pairedTv.text = "Paired Devices"
                val devices = bluetoothAdapter.bondedDevices
                for (device in devices) {
                    val deviceName = device.name
                    binding.pairedTv.append("\nDevice: $deviceName, $device")
                }
            } else {
                Toast.makeText(this, "Turn on bluetooth first", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH
                ),
                1
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    binding.bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
                    Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Can not access to bluetooth", Toast.LENGTH_LONG).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val REQUEST_CODE_ENABLE_BT = 1
        const val REQUEST_CODE_DISCOVERABLE_BT = 2
    }
}