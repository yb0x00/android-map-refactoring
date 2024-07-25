package campus.tech.kakao.map.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.R
import campus.tech.kakao.map.data.room.SearchHistoryData
import campus.tech.kakao.map.listener.RecentAdapterListener
import campus.tech.kakao.map.viewModel.DBViewModel
import kotlinx.coroutines.launch

class RecentSearchAdapter(
    private val searchHistoryDataList: List<SearchHistoryData>,
    private val viewModel: DBViewModel,
    private val adapterListener: RecentAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RecentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.recent_search)
        val deleteBtn: ImageButton = view.findViewById(R.id.delete_recent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recent_search_item, parent, false)
        return RecentViewHolder(view)
    }

    override fun getItemCount(): Int = searchHistoryDataList.count()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = searchHistoryDataList[position]
        val viewHolder = holder as RecentViewHolder
        viewHolder.name.text = item.name

        viewHolder.deleteBtn.setOnClickListener {
            Log.d("yeong","Adapter: delete 버튼 누름")
            viewModel.viewModelScope.launch {
                viewModel.deleteRecentData(item.name, item.address, item.searchTime)
            }
        }

        viewHolder.name.setOnClickListener {
            adapterListener.autoSearch(item.name)
        }
    }
}