package com.nasen.railwaywatcher.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.Form
import com.nasen.railwaywatcher.Global
import com.nasen.railwaywatcher.R
import com.nasen.railwaywatcher.type.Railway
import kotlinx.android.synthetic.main.fragment_new_record.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NewRecordFragment : Fragment(), DatePickerFragment.Listener {
    private val args: NewRecordFragmentArgs by navArgs()
    lateinit var railway: Railway
    lateinit var recordForm: Form
    lateinit var sdf: DateFormat
    private var up: Boolean? = null
    private var date: Calendar? = Calendar.getInstance()
        set(value) {
            field = value
            dateText.text = if (value == null) {
                getString(R.string.none)
            } else {
                sdf.format(value.time)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        railway = Global.get(args.which)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.confirm, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.confirm -> {
            val result = recordForm.validate()
            if (result.success()) {
                val start = result["startKm"]!!.asInt()!! * 1000 + result["startM"]!!.asInt()!!
                val end = result["endKm"]!!.asInt()!! * 1000 + result["endM"]!!.asInt()!!
                if (start >= end) {
                    ValidationFailDialog(getString(R.string.start_pos_bigger_than_end_pos)).show(
                        childFragmentManager,
                        null
                    )
                } else {
                    val range =
                        railway.ranges.find { range -> range.start <= start && range.end >= end }
                    if (range == null) {
                        ValidationFailDialog(getString(R.string.record_not_inside_range)).show(
                            childFragmentManager,
                            null
                        )
                    } else {
                        if (date == null) {
                            Global.removeRecord(args.which, start, end, up)
                        } else {
                            Global.addRecord(args.which, start, end, date!!.time, up)
                        }
                        findNavController().navigateUp()
                    }
                }
            } else {
                ValidationFailDialog(result.errors()[0].description).show(
                    childFragmentManager,
                    null
                )
            }
            true
        }
        else -> findNavController().navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_record, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sdf = SimpleDateFormat.getDateInstance()
        dateText.text = sdf.format(date!!.time)
        recordForm = form {
            useRealTimeValidation()
            input(recordStartKM, "startKm") {
                isNumber().atLeast(0)
            }
            input(recordStartM, "startM") {
                isNumber().atLeast(0).lessThan(1000)
            }
            input(recordEndKm, "endKm") {
                isNumber().atLeast(0)
            }
            input(recordEndM, "endM") {
                isNumber().atLeast(0).lessThan(1000)
            }
        }
        setRecord.setOnClickListener {
            val dateFragment = DatePickerFragment(date)
            dateFragment.setTargetFragment(this, 0)
            dateFragment.show(parentFragmentManager, null)
        }
        setNoRecord.setOnClickListener {
            date = null
        }
        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.both -> up = null
                R.id.upstream -> up = true
                R.id.downstream -> up = false
                else -> {
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDialogResult(year: Int, month: Int, dayOfMonth: Int) {
        date = Calendar.Builder().setDate(year, month, dayOfMonth).build()
    }
}

class DatePickerFragment(val default: Calendar?) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {
    interface Listener {
        fun onDialogResult(year: Int, month: Int, dayOfMonth: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = default ?: Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        try {
            val listener = targetFragment as Listener
            listener.onDialogResult(year, month, dayOfMonth)
        } catch (e: ClassCastException) {
            throw IllegalStateException("Class" + targetFragment.toString() + "must implement Listener")
        }
    }
}
