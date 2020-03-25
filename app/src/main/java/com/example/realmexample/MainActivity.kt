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

    lateinit var realm: Realm
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
        val searchView =
            toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    fun updateAdapterItems(searchWord: String) {
        realm.beginTransaction()
        val realmResult: RealmResults<SearchVo> =
            realm.where(SearchVo::class.java).sort("writeAt", Sort.DESCENDING).findAll()
        items = realmResult
        realm.commitTransaction()
        realm.beginTransaction()
        val vo: SearchVo? =
            realm.where(SearchVo::class.java).equalTo("recentSearchWord", searchWord).findFirst()
        realm.commitTransaction()
        recyclerViewSearch.adapter?.let { adapter ->
            (adapter as SearchAdapter).unFilteredlist = realm.copyFromRealm(items)

            vo?.let {
                adapter.filteredList = adapter.unFilteredlist.filter {
                    it.recentSearchWord.toLowerCase().contains(searchWord)
                }.toMutableList()
//                adapter.filteredList.add(realm.copyFromRealm(vo))
            }
            adapter.notifyDataSetChanged()
        }

    }

    fun insertSearchVo(searchWord: String) {
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
                    var primaryKey = realm.where(SearchVo::class.java).max("id")?.toLong()
                    primaryKey = when (primaryKey) {
                        null -> 0
                        else -> primaryKey + 1
                    }
                    val vo: SearchVo = SearchVo()
                    vo.id = primaryKey
                    vo.recentSearchWord = searchWord
                    vo.writeAt = currentTime
                    realm.copyToRealm(vo)
                }
            }
        }, object : Realm.Transaction.OnSuccess {
            override fun onSuccess() {
                realm.refresh()
                updateAdapterItems(searchWord)
            }
        })
    }
}
