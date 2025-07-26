package com.lfgit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.lfgit.R
import com.lfgit.activities.RepoTasksActivity
import com.lfgit.view_models.RepoTasksViewModel

/**
 * Bind Git tasks to the task drawer
 *
 * source:
 * https://github.com/maks/MGit/blob/master/app/src/main/java/me/sheimi/sgit/adapters/RepoOperationsAdapter.java
 */
class RepoTasksAdapter(
    context: Context,
    private val mViewModel: RepoTasksViewModel
) : ArrayAdapter<RepoTasksAdapter.DrawerItem>(context, 0), AdapterView.OnItemClickListener {

    private val mRepoTasksActivity: RepoTasksActivity = context as RepoTasksActivity

    init {
        setupDrawerItem()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: newView(parent)
        bindView(view, position)
        return view
    }

    private fun newView(parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.drawer_list_item, parent, false)
        val holder = DrawerItemHolder(view.findViewById(R.id.name))
        view.tag = holder
        return view
    }

    private fun bindView(view: View, position: Int) {
        val holder = view.tag as DrawerItemHolder
        val item = getItem(position)
        holder.name.text = item?.name
    }

    data class DrawerItemHolder(val name: TextView)

    data class DrawerItem(val name: String)

    private fun setupDrawerItem() {
        val ops = context.resources.getStringArray(R.array.repo_tasks_array)
        ops.forEach { op -> add(DrawerItem(op)) }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        mRepoTasksActivity.closeDrawer()
        mViewModel.execGitTask(position)
    }
}