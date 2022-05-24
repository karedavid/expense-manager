package com.kdapps.kolteskoveto

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kdapps.kolteskoveto.data.SpendNode
import com.kdapps.kolteskoveto.data.UserDataRepository
import com.kdapps.kolteskoveto.databinding.ActivityNewBinding
import java.util.*
import kotlin.concurrent.thread


class NewActivity : AppCompatActivity(), LocationListener {

    companion object{
        const val EDITTAG = "editIdTag"
    }

    private lateinit var binding: ActivityNewBinding
    private lateinit var locationManager: LocationManager
    private var selectedDay: Calendar = Calendar.getInstance()
    private lateinit var editNode : SpendNode


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editId = intent.getLongExtra(EDITTAG, -1)
        binding = ActivityNewBinding.inflate(layoutInflater)
        locationManager = getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        //supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.md_theme_dark_surface)))
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            binding.btnGPS.isEnabled = false
        }

        binding.btnGPS.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                }
            } else {

                try {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        binding.etPlace.error = getString(R.string.et_gps_error)
                        binding.btnGPS.isEnabled = false
                        throw Exception()
                    }

                    binding.etPlace.error = null

                    val location =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        binding.etPlace.editText?.setText(addresses[0].getAddressLine(0))
                    }

                    if (location == null || location.time <= Calendar.getInstance().timeInMillis - 10000) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0F,
                            this
                        )
                    }
                } catch (e: Exception) {
                }
            }
        }

        binding.twCalendarTitle.text = getString(R.string.date_of_payment)

        binding.cbPaid.setOnCheckedChangeListener { _, isChecked ->
            binding.twCalendarTitle.text = when (isChecked) {
                true -> getString(R.string.date_of_payment)
                false -> getString(R.string.date_of_deadline)
            }
        }

        binding.cwDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDay.set(year, month, dayOfMonth)
        }

        if(editId != (-1).toLong()){
            onEditRequest(editId)
            supportActionBar?.title = getString(R.string.title_edit)
        }
    }

    private fun onEditRequest(itemId: Long){
        thread {
            editNode = UserDataRepository(application).getSpendNodeById(itemId)

            if(editNode != null){
                runOnUiThread { binding.etName.editText?.setText(editNode.name)
                    binding.etCat2.editText?.setText(editNode.secondary_category)
                    binding.etAmount.editText?.setText(editNode.amount.toString())
                    binding.etDesc.editText?.setText(editNode.desc)
                    binding.etPlace.editText?.setText(editNode.place)

                    binding.cbPaid.isChecked = editNode.paid
                    binding.cwDate.date = editNode.date
                    selectedDay.timeInMillis = editNode.date

                     when(editNode.main_category) {

                         SpendNode.CATEGORY_FOOD -> binding.chipFood.isChecked = true
                         SpendNode.CATEGORY_HOUSEHOLD -> binding.chipHousehold.isChecked = true
                         SpendNode.CATEGORY_LIVING -> binding.chipLiving.isChecked = true
                         SpendNode.CATEGORY_TRANSPORTATION -> binding.chipTransportation.isChecked = true
                         SpendNode.CATEGORY_OCCASIONAL -> binding.chipOccasional.isChecked = true
                         else -> binding.chipNonCat.isChecked = true
                    }
                }
            }

        }

    }

    private fun validateField(): Boolean {

        var result = true

        if (binding.etName.editText?.text?.isEmpty() == true) {
            binding.etName.error = getString(R.string.et_required_error)
            result = false
        } else {
            binding.etName.error = null
        }

        if (binding.etAmount.editText?.text?.isEmpty() == true) {
            binding.etAmount.error = getString(R.string.et_required_error)
            result = false
        } else {
            binding.etAmount.error = null
        }

        return result
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    binding.btnGPS.performClick()
                }
            } else {
                binding.btnGPS.isEnabled = false
            }
            return
        }
    }

    override fun onLocationChanged(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        binding.etPlace.editText?.setText(addresses[0].getAddressLine(0))
        locationManager.removeUpdates(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.new_menu_top, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.navigation_save_new && validateField()) {

            val name = binding.etName.editText?.text.toString().trim()
            val desc = binding.etDesc.editText?.text.toString().trim()
            val amount = Integer.parseInt(binding.etAmount.editText?.text.toString())
            val place = binding.etPlace.editText?.text.toString().trim()
            val paid = binding.cbPaid.isChecked
            val cat2 = binding.etCat2.editText?.text.toString().trim()

            val cat1: Int = when {
                binding.chipFood.isChecked -> SpendNode.CATEGORY_FOOD
                binding.chipHousehold.isChecked -> SpendNode.CATEGORY_HOUSEHOLD
                binding.chipLiving.isChecked -> SpendNode.CATEGORY_LIVING
                binding.chipTransportation.isChecked -> SpendNode.CATEGORY_TRANSPORTATION
                binding.chipOccasional.isChecked -> SpendNode.CATEGORY_OCCASIONAL
                else -> SpendNode.CATEGORY_OTHER
            }

            selectedDay.firstDayOfWeek = Calendar.MONDAY
            val year = selectedDay.get(Calendar.YEAR)
            val month = selectedDay.get(Calendar.MONTH)
            val day = selectedDay.get(Calendar.DAY_OF_MONTH)
            val week = selectedDay.get(Calendar.WEEK_OF_YEAR)

            if(intent.getLongExtra(EDITTAG, -1) != (-1).toLong()){
                editNode.name = name
                editNode.main_category = cat1
                editNode.secondary_category = cat2
                editNode.amount = amount
                editNode.date = selectedDay.timeInMillis
                editNode.year = year
                editNode.month = month
                editNode.day = day
                editNode.week = week
                editNode.paid = paid
                editNode.desc = desc
                editNode.place = place

                thread {
                    UserDataRepository(application).updateSpendNode(editNode)
                    finish()
                }

            }else{
                val newNode = SpendNode(
                    null,
                    null,
                    name,
                    cat1,
                    cat2,
                    amount,
                    selectedDay.timeInMillis,
                    year,
                    month,
                    day,
                    week,
                    paid,
                    desc,
                    place,
                    false
                )

                thread {
                    UserDataRepository(application).insertSpendNode(newNode)
                    finish()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}