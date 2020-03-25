/*
 * Copyright 2017 Arthur Ivanets, arthur.ivanets.l@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.realmexample.R
import com.example.realmexample.SearchVo
import io.realm.Realm
import kotlinx.android.synthetic.main.item_recyclerview_textview.view.*
import java.util.*
import kotlin.collections.ArrayList


class SearchAdapter(items: MutableList<SearchVo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {

    var unFilteredlist: MutableList<SearchVo>
    var filteredList: MutableList<SearchVo>

    init {
        unFilteredlist = items
        filteredList = items
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()

                filteredList = when (charString.isEmpty()) {
                    true -> unFilteredlist
                    false -> {
                        unFilteredlist
                            .filter { (it.recentSearchWord.toLowerCase()).contains(charString.toLowerCase()) }.toMutableList()
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                filterResults.count = filteredList.size
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                if(results.values != null){
                    filteredList = (results.values)  as MutableList<SearchVo>
                    notifyDataSetChanged()
                }else{

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TextViewItemViewHolder(
            itemView = inflater.inflate(
                R.layout.item_recyclerview_textview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TextViewItemViewHolder).bindData(getItem(position))

    }

    fun getItem(position: Int): String {
        return filteredList[position].recentSearchWord
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }


    inner class TextViewItemViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val textViewSearchWord = itemView.textView

        init {

        }

        fun bindData(item: String) {
            textViewSearchWord.text = item
        }
    }
}
