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


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.realmexample.R
import com.example.realmexample.SearchVo
import kotlinx.android.synthetic.main.item_recyclerview_textview.view.*
import java.text.SimpleDateFormat
import java.util.*


class SearchAdapter(items: MutableList<SearchVo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {

    var unFilteredList: MutableList<SearchVo> = items
    var filteredList: MutableList<SearchVo>

    var currentText = ""

    init {
        filteredList = items
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()

                currentText = charString

                filteredList = when (charString.isEmpty()) {
                    true -> unFilteredList
                    false -> {
                        unFilteredList
                            .filter { (it.recentSearchWord.toLowerCase()).contains(charString.toLowerCase()) }
                            .toMutableList()
                    }
                }

                return FilterResults().apply {
                    this.values = filteredList
                    this.count = filteredList.size
                }
            }

            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                results.values?.let {
                    filteredList = it as MutableList<SearchVo>
                    notifyDataSetChanged()
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

    private fun getItem(position: Int): SearchVo {
        return filteredList[position]
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }


    inner class TextViewItemViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val textViewSearchWord = itemView.textViewRecentSearchWord
        private val textViewWriteAt = itemView.textViewWriteAt

//        init {
//
//        }

        fun bindData(item: SearchVo) {
            val recentSearchWord : String = item.recentSearchWord
            val writeAt : String = SimpleDateFormat("E MMM dd HH:mm:ss", Locale.KOREA).format(item.writeAt)
            val spannableStringBuilder = SpannableStringBuilder(item.recentSearchWord)

            var indexStart: Int = recentSearchWord.indexOf(currentText)
            while (indexStart >= 0 && currentText.isNotEmpty()) {
                val indexEnd : Int = indexStart + currentText.length
                spannableStringBuilder.setSpan(
                    ForegroundColorSpan(Color.RED),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                indexStart = recentSearchWord.indexOf(currentText, indexStart + 1)
            }
            textViewSearchWord.text = spannableStringBuilder
            textViewWriteAt.text = writeAt
        }
    }
}
