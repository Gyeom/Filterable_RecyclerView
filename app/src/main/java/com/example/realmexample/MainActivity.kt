package com.example.realmexample

import SearchAdapter
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var items: MutableList<SearchVo>
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realm = initializeRealm()

        realm.beginTransaction()
        val realmResult: RealmResults<SearchVo> =
            realm.where(SearchVo::class.java).sort("writeAt", Sort.DESCENDING).findAll()
        items = realm.copyFromRealm(realmResult)
        realm.commitTransaction()


        recyclerViewSearch.adapter = SearchAdapter(items)
        recyclerViewSearch.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        initializeSearchView()

    }

    private fun initializeRealm(): Realm {
        Realm.init(this)
        return Realm.getDefaultInstance()
    }

    private fun initializeSearchView() {
        toolbar.inflateMenu(R.menu.menu_search)
        val searchView: SearchView =
            toolbar.menu.findItem(R.id.action_search).actionView as SearchView

        with(searchView) {
            maxWidth = Integer.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(searchWord: String): Boolean {
                    insertSearchVo(searchWord)
                    return true
                }

                override fun onQueryTextChange(searchWord: String): Boolean {
                    (recyclerViewSearch.adapter as SearchAdapter).filter.filter(searchWord)
                    return true
                }
            })
        }
    }

    private fun updateAdapterItems(addItem: SearchVo) {

        items = selectSearchVos()

        val adapter: SearchAdapter = (recyclerViewSearch.adapter as SearchAdapter).apply {
            this.unFilteredList = realm.copyFromRealm(items)
            this.filteredList = unFilteredList.filter { searchVo ->
                searchVo.recentSearchWord.toLowerCase().contains(addItem.recentSearchWord)
            }.toMutableList()
        }

        adapter.notifyDataSetChanged()
    }

    private fun selectSearchVos(): RealmResults<SearchVo> {
        return realm.where(SearchVo::class.java).sort("writeAt", Sort.DESCENDING).findAll()
    }

    private fun insertSearchVo(searchWord: String) {
        val addItem = SearchVo()

        realm.executeTransactionAsync(object : Realm.Transaction {
            override fun execute(realm: Realm) {
                val currentTime = Date(System.currentTimeMillis())

                realm.where(SearchVo::class.java).equalTo(
                    "recentSearchWord",
                    searchWord
                ).findFirst()?.let {
                    it.writeAt = currentTime
                    realm.copyToRealm(it)
                } ?: run {
                    var primaryKey: Long? = realm.where(SearchVo::class.java).max("id")?.toLong()
                    primaryKey = when (primaryKey) {
                        null -> 0
                        else -> primaryKey + 1
                    }
                    addItem.apply {
                        this.id = primaryKey
                        this.recentSearchWord = searchWord
                        this.writeAt = currentTime
                    }

                    realm.copyToRealm(addItem)
                }
            }
        }, Realm.Transaction.OnSuccess {
            realm.refresh()
            updateAdapterItems(addItem)
        })
    }
}
