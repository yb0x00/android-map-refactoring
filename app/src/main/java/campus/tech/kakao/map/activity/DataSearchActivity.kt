package campus.tech.kakao.map.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.R
import campus.tech.kakao.map.listener.RecentAdapterListener
import campus.tech.kakao.map.listener.SearchAdapterListener
import campus.tech.kakao.map.adapter.RecentSearchAdapter
import campus.tech.kakao.map.adapter.SearchDataAdapter
import campus.tech.kakao.map.dataContract.LocationDataContract
import campus.tech.kakao.map.dataRepository.SearchHistoryRepository
import campus.tech.kakao.map.viewModel.DBViewModel
import campus.tech.kakao.map.viewModel.SearchViewModel
import campus.tech.kakao.map.viewModel.factory.DBViewModelFactory

class DataSearchActivity : AppCompatActivity(), RecentAdapterListener, SearchAdapterListener {
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var recentViewModel: DBViewModel
    private lateinit var editText: EditText
    private lateinit var resultDataAdapter: SearchDataAdapter
    private lateinit var searchDataListView: RecyclerView
    private lateinit var recentSearchListView: RecyclerView
    private lateinit var noResultNotice: TextView
    private lateinit var deleteBtn: ImageButton
    private lateinit var dbRepo: SearchHistoryRepository


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_search)

        //View 초기화
        searchDataListView = findViewById(R.id.searchResulListView)
        editText = findViewById(R.id.searchBar)
        noResultNotice = findViewById(R.id.noResult)
        deleteBtn = findViewById(R.id.deleteInput)
        recentSearchListView = findViewById(R.id.recentSearchListView)

        //ViewModel 생성
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        dbRepo = SearchHistoryRepository(this)
        recentViewModel =
            ViewModelProvider(this, DBViewModelFactory(dbRepo))[DBViewModel::class.java]

        //검색 결과 목록 세로 스크롤 설정
        searchDataListView.layoutManager = LinearLayoutManager(this)
        //최근 검색어 목록 가로 스크롤 설정
        recentSearchListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //어뎁터 초기화
        resultDataAdapter = SearchDataAdapter(emptyList(), recentViewModel, this)
        searchDataListView.adapter = resultDataAdapter

        resetButtonListener()
        setTextWatcher()

        recentViewModel.getRecentDataLiveData().observe(this, Observer { recentData ->
            recentSearchListView.adapter = RecentSearchAdapter(recentData, recentViewModel, this)
        })

        searchViewModel.searchResults.observe(this, Observer { documentsList ->
            if (documentsList.isNotEmpty()) {
                noResultNotice.visibility = View.GONE
                resultDataAdapter.updateData(documentsList)
            } else {
                noResultNotice.visibility = View.VISIBLE
                resultDataAdapter.updateData(emptyList())
            }
        })

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@DataSearchActivity, HomeMapActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setTextWatcher() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchInput = editText.text.trim().toString()

                if (searchInput.isNotEmpty()) {
                    searchViewModel.loadResultData(searchInput)
                } else {
                    noResultNotice.visibility = View.VISIBLE
                    resultDataAdapter.updateData(emptyList())
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun resetButtonListener() {
        deleteBtn.setOnClickListener {
            editText.text.clear()
        }
    }

    //클릭한 검색어가 자동으로 입력되는 기능 구현
    override fun autoSearch(searchData: String) {
        editText.setText(searchData)
    }

    override fun displaySearchLocation(
        name: String,
        address: String,
        latitude: String,
        longitude: String
    ) {
        val intent = Intent(this@DataSearchActivity, HomeMapActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(LocationDataContract.LOCATION_NAME, name)
        intent.putExtra(LocationDataContract.LOCATION_ADDRESS, address)
        intent.putExtra(LocationDataContract.LOCATION_LATITUDE, latitude)
        intent.putExtra(LocationDataContract.LOCATION_LONGITUDE, longitude)
        startActivity(intent)
    }
}
