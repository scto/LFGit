package com.lfgit.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView

import com.lfgit.R
import com.lfgit.activities.BasicAbstractActivity
import com.lfgit.activities.RepoTasksActivity
import com.lfgit.database.model.Repo
import com.lfgit.view_models.RepoListViewModel

import org.apache.commons.lang3.StringUtils

/** Bind repositories to the view. */
class RepoListAdapter(
    private val mContext: BasicAbstractActivity,
    private val mRepoListViewModel: RepoListViewModel
) : ArrayAdapter<Repo>(mContext, 0), AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    private var mLastRepoList: MutableList<Repo>? = null

    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
        val repo = getItem(position) ?: return
        if (mRepoListViewModel.repoDirExists(repo)) {
            val intent = Intent(mContext, RepoTasksActivity::class.java).apply {
                putExtra(Repo.TAG, repo)
            }
            mContext.startActivity(intent)
        } else {
            mContext.showToastMsg(mContext.resources.getString(R.string.repo_not_found))
            mLastRepoList?.let {
                it.remove(repo)
                setRepos(it)
                removeRepoDB(repo)
            }
        }
    }

    override fun onItemLongClick(
        adapterView: AdapterView<*>,
        view: View,
        position: Int,
        id: Long
    ): Boolean {
        val dialog = arrayOf(
            BasicAbstractActivity.onOptionClicked { deleteRepo(position) }
        )
        mContext.showOptionsDialog(R.string.dialog_choose_option, R.array.repo_options, dialog)
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: newView(parent)
        bindView(view, position)
        return view
    }

    private fun newView(parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.repo_list_item, parent, false)
        view.tag = RepoListItemHolder(
            title = view.findViewById(R.id.title),
            remoteURL = view.findViewById(R.id.remoteURL),
            localPath = view.findViewById(R.id.localPath)
        )
        return view
    }

    private fun bindView(view: View, position: Int) {
        val holder = view.tag as RepoListItemHolder
        val repo = getItem(position)
        if (repo != null) {
            holder.title.text = repo.displayName
            holder.localPath.text = repo.localPath
            holder.remoteURL.text = if (StringUtils.isNotBlank(repo.remoteURL)) {
                repo.remoteURL
            } else {
                mContext.resources.getString(R.string.unknown_remote)
            }
        }
    }

    fun setRepos(repos: List<Repo>?) {
        repos ?: return
        mLastRepoList = repos.toMutableList()
        setExistingRepos(repos)
    }

    private fun setExistingRepos(repos: List<Repo>?) {
        repos ?: return
        // if repository directory exists add it to the list
        val validRepos = repos.filter { repo ->
            if (mRepoListViewModel.repoDirExists(repo)) {
                true
            } else {
                removeRepoDB(repo)
                false
            }
        }

        clear()
        addAll(validRepos)
        notifyDataSetChanged()
    }

    fun refreshRepos() {
        mLastRepoList?.let { setExistingRepos(it) }
    }

    private fun deleteRepo(position: Int) {
        getItem(position)?.let { removeRepoDB(it) }
    }

    private fun removeRepoDB(repo: Repo) {
		//remove(repo)
        remove(repo.id)
        notifyDataSetChanged()
        mRepoListViewModel.deleteRepoById(repo.id)
    }

    private data class RepoListItemHolder(
        val title: TextView,
        val localPath: TextView,
        val remoteURL: TextView
    )
}