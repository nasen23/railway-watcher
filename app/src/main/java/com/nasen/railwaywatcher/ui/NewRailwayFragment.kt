package com.nasen.railwaywatcher.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.Form
import com.nasen.railwaywatcher.R
import kotlinx.android.synthetic.main.fragment_new_railway.*

class NewRailwayFragment : Fragment() {
    lateinit var myForm: Form

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_new_railway, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.confirm, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.confirm -> validateRangeAndNext()
        else -> Navigation.findNavController(requireView()).navigateUp()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myForm = form {
            useRealTimeValidation()
            input(nameText, "name") {
                isNotEmpty()
            }
            input(startKmText, "startKm") {
                isNumber().atLeast(0)
            }
            input(startMText, "startM") {
                isNumber().atLeast(0).lessThan(1000)
            }
            input(endKmText, "endKm") {
                isNumber().atLeast(0)
            }
            input(endMText, "endM") {
                isNumber().atLeast(0).lessThan(1000)
            }
        }
    }

    private fun validateRangeAndNext(): Boolean {
        val result = myForm.validate()
        if (result.hasErrors()) {
            val fragment = ValidationFailDialog(result.errors()[0].description)
            fragment.show(childFragmentManager, "validation_fail")
            return false
        }
        val startPos =
            result["startKm"]!!.asInt()!! * 1000 + result["startM"]!!.asInt()!!
        val endPos = result["endKm"]!!.asInt()!! * 1000 + result["endM"]!!.asInt()!!
        if (startPos >= endPos) {
            val fragment = ValidationFailDialog(getString(R.string.start_pos_bigger_than_end_pos))
            fragment.show(childFragmentManager, null)
            return false
        }
        val name = result["name"]!!.asString()
        val action = NewRailwayFragmentDirections.actionNewRailwayFragmentToNewSubRangeFragment(
            startPos,
            endPos,
            name
        )
        Navigation.findNavController(requireView())
            .navigate(action)
        return true
    }

}