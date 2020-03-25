package com.example.realmexample

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class SearchVo(

    @PrimaryKey
    var id: Long = 0,
    var recentSearchWord : String = "",
    var writeAt : Date = Date(System.currentTimeMillis())) : RealmObject()