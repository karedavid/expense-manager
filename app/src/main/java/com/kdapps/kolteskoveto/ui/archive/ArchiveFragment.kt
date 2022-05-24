package com.kdapps.kolteskoveto.ui.archive

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kdapps.kolteskoveto.R
import com.kdapps.kolteskoveto.SettingsActivity
import com.kdapps.kolteskoveto.data.ArchiveNode
import com.kdapps.kolteskoveto.databinding.FragmentArchiveBinding
import java.util.*
import kotlin.concurrent.thread


class ArchiveFragment : Fragment(), ArchiveAdapter.ArchiveNodeClickListener {

    private lateinit var archiveViewModel: ArchiveViewModel
    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        archiveViewModel = ViewModelProvider(this).get(ArchiveViewModel::class.java)

        _binding = FragmentArchiveBinding.inflate(inflater, container, false)

        val rvAdapter = ArchiveAdapter(requireActivity(), archiveViewModel, this)
        binding.archiveRecyclerView.adapter = rvAdapter
        (binding.archiveRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.archiveRecyclerView.setHasFixedSize(true)

        archiveViewModel.allArchiveNode.observe(viewLifecycleOwner) {
            rvAdapter.setNodes(it)
        }

        archiveViewModel.allArchiveSum.observe(viewLifecycleOwner) {
            rvAdapter.setArchiveSum(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemDeleted(item: ArchiveNode) {
        archiveViewModel.deleteArchive(item)
    }

    override fun onItemUnarchived(item: ArchiveNode) {
        archiveViewModel.unarchiveArchive(item)
    }

    override fun onItemSend(item: ArchiveNode) {
        thread {
            val text = item.id?.let { archiveViewModel.getArchivedNodeDescription(it) }
            val subject = item.name ?.let { item.name } ?: getString(R.string.title_archive)
            requireActivity().runOnUiThread {
                if (text != null) {
                    composeEmail(text, subject)
                }
            }
        }
    }

    private fun composeEmail(text: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as AppCompatActivity).setSupportActionBar(binding.topAppBar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dashboard_menu_top, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}