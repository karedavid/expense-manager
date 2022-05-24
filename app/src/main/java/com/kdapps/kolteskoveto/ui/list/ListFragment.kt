package com.kdapps.kolteskoveto.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kdapps.kolteskoveto.NewActivity
import com.kdapps.kolteskoveto.R
import com.kdapps.kolteskoveto.SettingsActivity
import com.kdapps.kolteskoveto.data.SpendNode
import com.kdapps.kolteskoveto.databinding.FragmentListBinding

class ListFragment : Fragment(), ListAdapter.SpendNodeClickListener {

    private lateinit var listViewModel: ListViewModel

    private var _binding            : FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvAdapter  : ListAdapter

    private lateinit var allActiveSpendNodeObserver : Observer<List<SpendNode>>
    private lateinit var filteredSpendNodeObserver  : Observer<List<SpendNode>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        rvAdapter = ListAdapter(requireContext(), this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // View Model
        listViewModel = ViewModelProvider(this).get(ListViewModel::class.java)

        // View Binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Recycler View
        binding.listRecyclerView.adapter = rvAdapter
        (binding.listRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.listRecyclerView.setHasFixedSize(true)

        allActiveSpendNodeObserver = Observer {
            rvAdapter.setNodes(it)
        }

        filteredSpendNodeObserver = Observer {
            rvAdapter.setNodes(it)
        }

        listViewModel.allActiveSpendNode.observe(viewLifecycleOwner, allActiveSpendNodeObserver)
        listViewModel.filteredSpendNode.observe(viewLifecycleOwner, filteredSpendNodeObserver)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.list_menu_top, menu)

        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.setOnItemReselectedListener {
            if (it.itemId == R.id.navigation_list) {
                menu.getItem(1).expandActionView()
            }
        }

        val searchView = menu.findItem(R.id.navigation_search)?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                listViewModel.filterQuery.value = newText
                return false
            }
        })

        menu.getItem(1).setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                listViewModel.allActiveSpendNode.removeObserver(allActiveSpendNodeObserver)
                listViewModel.filteredSpendNode.observe(viewLifecycleOwner, filteredSpendNodeObserver)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                listViewModel.filteredSpendNode.removeObserver( filteredSpendNodeObserver)
                listViewModel.allActiveSpendNode.observe(viewLifecycleOwner, allActiveSpendNodeObserver)
                return true
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as AppCompatActivity).setSupportActionBar(binding.topAppBar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Data manipulation

    override fun onItemDeleted(item: SpendNode) {
        listViewModel.deleteSpendNode(item)
    }

    override fun onItemUpdated(item: SpendNode) {
        listViewModel.updateSpendNode(item)
    }

    override fun onItemEdited(item: SpendNode) {
        startActivity(Intent(requireActivity(), NewActivity::class.java).putExtra(NewActivity.EDITTAG,item.id))
    }


}